/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for mapping annotated persistent objects to and from the JSON representations of the Salesforce generic REST
 * API.
 * <p/>
 * This context includes the basic Jackson {@link ObjectMapper} configured appropriately as extra metadata in the form
 * of {@link ObjectDescriptor} for use in making other advanced choices related to persistence.
 */
final class ObjectMappingContext {

    private final ObjectMapper objectMapper;
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriterForCreate;
    private final ObjectWriter objectWriterForUpdate;
    private final ObjectWriter objectWriterForPatch;

    private final Map<Class<?>, ObjectDescriptor> descriptors = new ConcurrentHashMap<Class<?>, ObjectDescriptor>();
    private final Map<Class<?>, ObjectDescriptor> incompleteDescriptors = new HashMap<Class<?>, ObjectDescriptor>();

    public ObjectMappingContext() {
        objectMapper = newConfiguredObjectMapper();

        objectReader = objectMapper.reader();
        objectWriterForCreate = objectMapper.writerWithView(SerializationViews.Create.class);
        objectWriterForPatch = objectMapper.writerWithView(SerializationViews.Patch.class);

        // Unfortunately we need to create a completely new object mapper just for the alternate serialization
        // inclusion setting of "update". Trying to share or copy the same mapper ends up getting the wrong stuff
        // from the cache. Maybe a future change in Jackson will help.
        ObjectMapper objectMapper2 = newConfiguredObjectMapper();
        objectMapper2.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectWriterForUpdate = objectMapper2.writerWithView(SerializationViews.Update.class);
    }

    private ObjectMapper newConfiguredObjectMapper() {
        DefaultDeserializationContext deserializationContext = new DefaultDeserializationContext.Impl(new SubqueryDeserializerFactory());

        CustomObjectMapper objectMapper = new CustomObjectMapper(null, null, deserializationContext);

        objectMapper.setSerializationPropertyNamingStrategy(new RelationshipNamingStrategy(false));
        objectMapper.setDeserializationPropertyNamingStrategy(new RelationshipNamingStrategy(true));
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JodaModule());
        objectMapper.setAnnotationIntrospector(new SpaAnnotationIntrospector(this));

        return objectMapper;
    }

    public ObjectReader getObjectReader() {
        return objectReader;
    }

    public ObjectWriter getObjectWriterForCreate() {
        return objectWriterForCreate;
    }

    public ObjectWriter getObjectWriterForPatch() {
        return objectWriterForPatch;
    }

    public ObjectWriter getObjectWriterForUpdate() {
        return objectWriterForUpdate;
    }

    /**
     * Gets the {@link ObjectDescriptor} for the specified class.
     * <p/>
     * If the class has already been seen then an existing, cached descriptor is returned. Otherwise the class is
     * introspected to build a new descriptor and the new descriptor cached for future use.
     *
     * @param clazz the class for which a descriptor is desired
     * @return the descriptor
     */
    public ObjectDescriptor getRequiredObjectDescriptor(Class<?> clazz) {
        ObjectDescriptor descriptor = getObjectDescriptor(clazz);
        if (descriptor == null) {
            throw new IllegalArgumentException(
                String.format("%s can't be used as a record, probably because it isn't annotated", clazz.getName()));
        }
        return descriptor;
    }

    /**
     * Gets the {@link ObjectDescriptor} for the specified class.
     * <p/>
     * If the class has already been seen then an existing, cached descriptor is returned. Otherwise the class is
     * introspected to build a new descriptor and the new descriptor cached for future use.
     *
     * @param clazz the class for which a descriptor is desired
     * @return the descriptor or null if none applies to the object
     */
    public ObjectDescriptor getObjectDescriptor(Class<?> clazz) {
        ObjectDescriptor descriptor = descriptors.get(clazz);
        if (descriptor != null)
            return descriptor;

        if (clazz.isPrimitive() || isIntrinsicJavaPackage(clazz.getPackage()) || isJodaTimePackage(clazz.getPackage()))
            return null; // Primitive types can't be Salesforce objects and therefore have no descriptors.

        if (isEnum(clazz))
            return null; // Enums can't be Salesforce objects

        return createObjectDescriptor(clazz);
    }

    private ObjectDescriptor createObjectDescriptor(Class<?> clazz) {
        synchronized (incompleteDescriptors) { // Just one thread can create at a time. Creation doesn't happen often.

            // If we already have something under construction then return it. (Comes into play for recursive calls
            // when resolving related entities).
            if (incompleteDescriptors.containsKey(clazz))
                return incompleteDescriptors.get(clazz);

            boolean recursiveCall = incompleteDescriptors.size() > 0;
            try {
                JavaType type = objectMapper.getTypeFactory().constructType(clazz);
                BasicBeanDescription beanDescription = objectMapper.getDeserializationConfig().introspect(type);
                ObjectDescriptor descriptor =
                    new ObjectDescriptor(
                        getObjectName(beanDescription), beanDescription,
                        getIdProperty(beanDescription), getAttributesProperty(beanDescription));
                incompleteDescriptors.put(clazz, descriptor);

                // Resolve related descriptors recursively
                for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                    ObjectDescriptor relatedDescriptor = getObjectDescriptor(getPropertyClass(property));
                    if (relatedDescriptor != null)
                        descriptor.getRelatedObjects().put(property.getInternalName(), relatedDescriptor);
                }

                if (!recursiveCall)
                    for (Class<?> key : incompleteDescriptors.keySet())
                        descriptors.put(key, incompleteDescriptors.get(key));

                return descriptor;

            } finally {
                if (!recursiveCall)
                    incompleteDescriptors.clear();
            }
        }
    }

    private String getObjectName(BasicBeanDescription beanDescription) {
        AnnotationIntrospector introspector = objectMapper.getDeserializationConfig().getAnnotationIntrospector();
        String name = introspector.findTypeName(beanDescription.getClassInfo());
        if (!StringUtils.isEmpty(name))
            return name;

        return beanDescription.getClassInfo().getRawType().getSimpleName();
    }

    private static BeanPropertyDefinition getIdProperty(BasicBeanDescription beanDescription) {
        for (BeanPropertyDefinition property : beanDescription.findProperties()) {
            if (property.getName().equalsIgnoreCase("id")) {
                return property;
            }
        }
        return null;
    }

    private static BeanPropertyDefinition getAttributesProperty(BasicBeanDescription beanDescription) {
        for (BeanPropertyDefinition property : beanDescription.findProperties()) {
            if (property.getName().equals("attributes")) {
                if (Map.class.isAssignableFrom(getPropertyClass(property))) {
                    return property;
                }
            }
        }
        return null;
    }

    private static boolean isIntrinsicJavaPackage(Package aPackage) {
        return (aPackage != null) && (aPackage.getName().startsWith("java."));
    }

    private static boolean isJodaTimePackage(Package aPackage) {
        return (aPackage != null) && (aPackage.getName().startsWith("org.joda.time"));
    }

    /*
     * An enhanced check for "isEnum" because the standard Class.isEnum() isn't always enough. When an enum includes
     * abstract methods an inner anonymous class arises and even though that class has the enum modifier bit set,
     * Class.isEnum() returns false which is not the answer we need.
     */
    private static boolean isEnum(Class<?> clazz) {
        return ((clazz.getModifiers() & 0x4000)) != 0;
    }

    private static Class<?> getPropertyClass(BeanPropertyDefinition property) {
        if (property.hasSetter()) {
            return getPropertyClass(property.getSetter().getGenericParameterType(0));
        } else if (property.hasField()) {
            return getPropertyClass(property.getField().getGenericType());
        } else
            throw new IllegalArgumentException("I don't know how to deal with that kind of property definition");
    }

    /*
     * Gets the class of the related object. If the class of the property is an array or collection then it is the class
     * of the contained elements that is relevant. Otherwise the property class is the one we want.
     */
    private static Class<?> getPropertyClass(Type propertyType) {
        if (propertyType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) propertyType;
            if (Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return getPropertyClass(parameterizedType.getActualTypeArguments()[0]); // Return the element class
            } else {
                return getPropertyClass(parameterizedType.getRawType());
            }

        } else if (propertyType instanceof Class) {
            Class<?> propertyClass = (Class<?>) propertyType;
            if (propertyClass.isArray()) {
                return propertyClass.getComponentType(); // Return the element class
            } else {
                return propertyClass;
            }
        } else {
            throw new IllegalArgumentException("I don't know how to deal with that kind of property definition");
        }
    }

    /**
     * A small extension to the standard Jackson ObjectMapper that is just a temporary hack so that we can set
     * asymmetric property naming strategies (different for serialization and deserialization). Unfortunately Jackson
     * 2.x changed ObjectMapper configuration in a way which prevented the public techniques that I used in 1.9.x to do
     * this.
     */
    private static class CustomObjectMapper extends ObjectMapper {
        CustomObjectMapper(JsonFactory jf, DefaultSerializerProvider sp, DefaultDeserializationContext dc) {
            super(jf, sp, dc);
        }

        public CustomObjectMapper setSerializationPropertyNamingStrategy(PropertyNamingStrategy strategy) {
            _serializationConfig = _serializationConfig.with(strategy);

            return this;
        }

        public CustomObjectMapper setDeserializationPropertyNamingStrategy(PropertyNamingStrategy strategy) {
            _deserializationConfig = _deserializationConfig.with(strategy);

            return this;
        }
    }
}
