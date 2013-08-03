/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.force.spa.RecordAccessorConfig;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for mapping annotated persistent objects to and from the JSON representations of the Salesforce generic REST
 * API.
 * <p/>
 * This context includes the basic Jackson {@link ObjectMapper} configured appropriately as extra metadata in the form
 * of {@link ObjectDescriptor} for use in making other advanced choices related to persistence.
 */
public final class ObjectMappingContext {
    private final ObjectMapper objectMapper;
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriterForCreate;
    private final ObjectWriter objectWriterForUpdate;
    private final ObjectWriter objectWriterForPatch;

    private final Map<Class<?>, ObjectDescriptor> descriptors = new ConcurrentHashMap<Class<?>, ObjectDescriptor>();
    private final Map<Class<?>, ObjectDescriptor> incompleteDescriptors = new HashMap<Class<?>, ObjectDescriptor>();

    public ObjectMappingContext() {
        this(new RecordAccessorConfig());
    }

    public ObjectMappingContext(RecordAccessorConfig config) {
        objectMapper = newConfiguredObjectMapper(config);

        objectReader = objectMapper.reader();
        objectWriterForCreate = objectMapper.writerWithView(SerializationViews.Create.class);
        objectWriterForPatch = objectMapper.writerWithView(SerializationViews.Patch.class);

        // Unfortunately we need to create a completely new object mapper just for the alternate serialization
        // inclusion setting of "update". Trying to share or copy the same mapper ends up getting the wrong stuff
        // from the cache. Maybe a future change in Jackson will help.
        ObjectMapper objectMapper2 = newConfiguredObjectMapper(config);
        objectMapper2.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectWriterForUpdate = objectMapper2.writerWithView(SerializationViews.Update.class);
    }

    private ObjectMapper newConfiguredObjectMapper(RecordAccessorConfig config) {
        DefaultDeserializationContext deserializationContext = new DefaultDeserializationContext.Impl(new SubqueryDeserializerFactory());

        ObjectMapper objectMapper = new ObjectMapper(null, null, deserializationContext);

        objectMapper.setSerializerFactory(new RelationshipAwareBeanSerializerFactory(this));
        objectMapper.setPropertyNamingStrategy(new RelationshipPropertyNamingStrategy(this));
        objectMapper.setAnnotationIntrospector(new SpaAnnotationIntrospector(config));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JodaModule());

        if (!config.isFieldAutodetectEnabled()) {
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        }

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
                String.format("%s can't be used as an object, probably because it isn't annotated", clazz.getName()));
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
                SerializationConfig config = objectMapper.getSerializationConfig();
                JavaType type = objectMapper.getTypeFactory().constructType(clazz);
                BasicBeanDescription beanDescription = config.introspect(type);

                List<FieldDescriptor> fields = new ArrayList<FieldDescriptor>();
                for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                    if (isPolymorphic(property, config)) {
                        fields.add(new FieldDescriptor(property, getSubtypes(property, config)));
                    } else {
                        fields.add(new FieldDescriptor(property));
                    }
                }

                ObjectDescriptor descriptor = new ObjectDescriptor(getObjectName(beanDescription), fields);
                incompleteDescriptors.put(clazz, descriptor);

                // Resolve related descriptors recursively
                for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                    ObjectDescriptor relatedDescriptor = getObjectDescriptor(getPropertyClass(beanDescription, property));
                    if (relatedDescriptor != null)
                        descriptor.getRelatedObjects().put(property.getInternalName(), relatedDescriptor);
                }

                if (!recursiveCall)
                    for (Class<?> descriptorClass : incompleteDescriptors.keySet())
                        descriptors.put(descriptorClass, incompleteDescriptors.get(descriptorClass));

                return descriptor;

            } finally {
                if (!recursiveCall)
                    incompleteDescriptors.clear();
            }
        }
    }

    private static boolean isPolymorphic(BeanPropertyDefinition property, MapperConfig<?> config) {
        return config.getAnnotationIntrospector().findPropertyTypeResolver(config, property.getAccessor(), null) != null;
    }

    private static Collection<NamedType> getSubtypes(BeanPropertyDefinition property, MapperConfig<?> config) {
        return config.getSubtypeResolver().collectAndResolveSubtypes(
            property.getAccessor(), config, config.getAnnotationIntrospector(), null);
    }

    private String getObjectName(BasicBeanDescription beanDescription) {
        AnnotationIntrospector introspector = objectMapper.getDeserializationConfig().getAnnotationIntrospector();
        String name = introspector.findTypeName(beanDescription.getClassInfo());
        if (!StringUtils.isEmpty(name))
            return name;

        return beanDescription.getClassInfo().getRawType().getSimpleName();
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

    private Class<?> getPropertyClass(BasicBeanDescription beanDescription, BeanPropertyDefinition property) {
        JavaType beanType = beanDescription.getType();
        if (property.hasSetter()) {
            return getPropertyClass(beanType, property.getSetter().getGenericParameterType(0));
        } else if (property.hasField()) {
            return getPropertyClass(beanType, property.getField().getGenericType());
        } else
            throw new IllegalArgumentException("I don't know how to deal with that kind of property definition");
    }

    /*
     * Gets the class of the related object. If the class of the property is an array or collection then it is the class
     * of the contained elements that is relevant. Otherwise the property class is the one we want.
     */
    private Class<?> getPropertyClass(JavaType beanType, Type propertyType) {
        if (propertyType instanceof Class) {
            return getPropertyClassForClass(beanType, (Class<?>) propertyType);

        } else if (propertyType instanceof ParameterizedType) {
            return getPropertyClassForParameterizedType(beanType, (ParameterizedType) propertyType);

        } else if (propertyType instanceof TypeVariable) {
            return getPropertyClassForGenericVariable(beanType, (TypeVariable) propertyType);

        } else {
            throw new IllegalArgumentException("Don't know how to deal with that kind of property definition");
        }
    }

    @SuppressWarnings("UnusedParameters")
    private Class<?> getPropertyClassForClass(JavaType beanType, Class<?> clazz) {
        if (clazz.isArray()) {
            return clazz.getComponentType(); // Return element class
        } else {
            return clazz;
        }
    }

    private Class<?> getPropertyClassForParameterizedType(JavaType beanType, ParameterizedType parameterizedType) {
        if (Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            return getPropertyClass(beanType, parameterizedType.getActualTypeArguments()[0]); // Return element class
        } else {
            return getPropertyClass(beanType, parameterizedType.getRawType());
        }
    }

    private Class<?> getPropertyClassForGenericVariable(JavaType beanType, TypeVariable typeVariable) {
        Class<?> beanClass = beanType.getRawClass();
        Class<?> genericClass = (Class<?>) typeVariable.getGenericDeclaration();
        if (genericClass.isAssignableFrom(beanClass)) {
            JavaType[] boundTypes = objectMapper.getTypeFactory().findTypeParameters(beanClass, genericClass);
            TypeVariable[] typeVariables = genericClass.getTypeParameters();
            for (int i = 0; i < typeVariables.length; i++) {
                if (typeVariables[i].equals(typeVariable)) {
                    return boundTypes[i].getRawClass();
                }
            }
        }
        throw new RuntimeException(
            String.format(
                "Don't know how to map type variable to a concrete type: variable=%s, beanClass=%s, genericClass=%s",
                typeVariable.getName(), beanClass.getSimpleName(), genericClass.getSimpleName()));
    }
}
