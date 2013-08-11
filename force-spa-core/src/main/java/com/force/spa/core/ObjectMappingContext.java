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
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.SalesforceObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for mapping annotated persistent objects to and from the JSON representations of the Salesforce generic REST
 * API.
 * <p/>
 * This context includes the basic Jackson {@link ObjectMapper} configured appropriately as extra metadata in the form
 * of {@link ObjectDescriptor} for use in making other advanced choices related to persistence.
 */
public final class ObjectMappingContext {
    private final RecordAccessorConfig config;
    private final ObjectMapper objectMapper;
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriterForCreate;
    private final ObjectWriter objectWriterForUpdate;
    private final ObjectWriter objectWriterForPatch;

    private final Map<Class<?>, ObjectDescriptor> descriptors = new ConcurrentHashMap<Class<?>, ObjectDescriptor>();
    private final Map<Class<?>, ObjectDescriptor> incompleteDescriptors = new HashMap<Class<?>, ObjectDescriptor>();
    private final Set<Class<?>> rejectedClasses = new HashSet<Class<?>>();

    public ObjectMappingContext() {
        this(new RecordAccessorConfig());
    }

    public ObjectMappingContext(RecordAccessorConfig config) {
        this.config = config;

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

        ObjectMapper objectMapper = new ObjectMapper(null, null, deserializationContext);

        objectMapper.setSerializerFactory(new RelationshipAwareBeanSerializerFactory(this));
        objectMapper.setPropertyNamingStrategy(new RelationshipPropertyNamingStrategy(this));
        objectMapper.setAnnotationIntrospector(new SpaAnnotationIntrospector(this, config));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JodaModule());

        if (config.isFieldAnnotationRequired()) {
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
                String.format("%s can't be used as a Salesforce object, probably because it isn't annotated", clazz.getName()));
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

        if (rejectedClasses.contains(clazz)) {
            return null;
        }

        if (canBeSalesforceObject(clazz)) {
            return createObjectDescriptor(clazz);
        } else {
            rejectedClasses.add(clazz);
            return null;
        }
    }

    private boolean canBeSalesforceObject(Class<?> clazz) {
        if (clazz.isPrimitive() || isIntrinsicJavaPackage(clazz.getPackage()) || isJodaTimePackage(clazz.getPackage())) {
            return false;
        }

        if (isEnum(clazz)) {
            return false;
        }

        return hasSalesforceObjectAnnotation(clazz) || !config.isObjectAnnotationRequired();
    }

    private boolean hasSalesforceObjectAnnotation(Class<?> clazz) {
        return clazz.getAnnotation(SalesforceObject.class) != null;
    }

    /**
     * Create an object descriptor (and all of its related descriptors).
     * <p/>
     * We do a little extra work here to handle forward and backward recursive references.
     */
    private ObjectDescriptor createObjectDescriptor(Class<?> type) {
        synchronized (incompleteDescriptors) { // Just one thread can create at a time. Creation doesn't happen often.

            // If we already have something under construction then return it. (Comes into play for recursive calls
            // when resolving related entities).
            if (incompleteDescriptors.containsKey(type))
                return incompleteDescriptors.get(type);

            boolean recursiveCall = isRecursiveCall();
            try {
                BasicBeanDescription beanDescription = getBeanDescription(type);
                ObjectDescriptor descriptor = new ObjectDescriptor(findObjectName(beanDescription));
                incompleteDescriptors.put(type, descriptor);
                descriptor.initializeFields(buildFieldDescriptors(beanDescription));

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

    private boolean isRecursiveCall() {
        return incompleteDescriptors.size() > 0;
    }

    private BasicBeanDescription getBeanDescription(Class<?> type) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(type);
        return objectMapper.getSerializationConfig().introspect(javaType);
    }

    private String findObjectName(BasicBeanDescription beanDescription) {
        AnnotationIntrospector introspector = objectMapper.getDeserializationConfig().getAnnotationIntrospector();
        String name = introspector.findTypeName(beanDescription.getClassInfo());
        if (!StringUtils.isEmpty(name))
            return name;

        return beanDescription.getClassInfo().getRawType().getSimpleName();
    }

    private List<FieldDescriptor> buildFieldDescriptors(BasicBeanDescription beanDescription) {
        List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();
        for (BeanPropertyDefinition property : beanDescription.findProperties()) {
            Class<?> propertyClass = JacksonUtils.getPropertyClass(beanDescription, property);
            ObjectDescriptor relatedObject = getRelatedObject(propertyClass);
            List<ObjectDescriptor> polymorphicChoices = getPolymorphicChoices(property, propertyClass);
            fieldDescriptors.add(new FieldDescriptor(property, propertyClass, relatedObject, polymorphicChoices));
        }
        return fieldDescriptors;
    }

    private List<ObjectDescriptor> getPolymorphicChoices(BeanPropertyDefinition property, Class<?> propertyClass) {
        if (isPolymorphic(property)) {
            List<ObjectDescriptor> relatedObjects = new ArrayList<ObjectDescriptor>();
            for (NamedType subtype : getSubtypes(property)) {
                if (!propertyClass.equals(subtype.getType())) {
                    relatedObjects.add(getRequiredObjectDescriptor(subtype.getType()));
                }
            }
            return Collections.unmodifiableList(relatedObjects);
        } else {
            return Collections.emptyList();
        }
    }

    private ObjectDescriptor getRelatedObject(Class<?> propertyClass) {
        return getObjectDescriptor(propertyClass);
    }

    private boolean isPolymorphic(BeanPropertyDefinition property) {
        SerializationConfig config = objectMapper.getSerializationConfig();
        return config.getAnnotationIntrospector().findPropertyTypeResolver(config, property.getAccessor(), null) != null;
    }

    private Collection<NamedType> getSubtypes(BeanPropertyDefinition property) {
        SerializationConfig config = objectMapper.getSerializationConfig();
        return config.getSubtypeResolver().collectAndResolveSubtypes(
            property.getAccessor(), config, config.getAnnotationIntrospector(), null);
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
}
