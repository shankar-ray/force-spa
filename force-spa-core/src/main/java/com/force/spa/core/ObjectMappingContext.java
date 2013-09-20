/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import static com.force.spa.core.IntrospectionUtils.canBeSalesforceObject;
import static com.force.spa.core.IntrospectionUtils.getConcreteClass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

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
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.SalesforceObject;

/**
 * Context for mapping annotated persistent objects to and from the JSON representations of the Salesforce generic REST
 * API.
 * <p/>
 * This context includes the basic Jackson {@link ObjectMapper} configured appropriately as extra metadata in the form
 * of {@link ObjectDescriptor} for use in making other advanced choices related to persistence.
 */
public final class ObjectMappingContext implements Serializable {

    private static final long serialVersionUID = 138801020026711582L;

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
        objectMapper.setPropertyNamingStrategy(new RelationshipPropertyNamingStrategy());
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
     * @param type the class for which a descriptor is desired
     * @return the descriptor
     */
    public ObjectDescriptor getRequiredObjectDescriptor(Class<?> type) {
        ObjectDescriptor descriptor = getObjectDescriptor(type);
        if (descriptor == null) {
            throw new IllegalArgumentException(
                String.format("%s can't be used as a Salesforce object, probably because it isn't annotated", type.getName()));
        }
        return descriptor;
    }

    /**
     * Gets the {@link ObjectDescriptor} for the specified class.
     * <p/>
     * If the class has already been seen then an existing, cached descriptor is returned. Otherwise the class is
     * introspected to build a new descriptor and the new descriptor cached for future use.
     *
     * @param type the class for which a descriptor is desired
     * @return the descriptor or null if none applies to the object
     */
    public ObjectDescriptor getObjectDescriptor(Class<?> type) {
        ObjectDescriptor descriptor = descriptors.get(type);
        if (descriptor != null)
            return descriptor;

        if (rejectedClasses.contains(type)) {
            return null;
        }

        if (canBeSalesforceObject(type, config.isObjectAnnotationRequired())) {
            return createObjectDescriptor(type);
        } else {
            rejectedClasses.add(type);
            return null;
        }
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
                ObjectDescriptor descriptor = new ObjectDescriptor(findObjectName(beanDescription), isMetadataAware(beanDescription));
                incompleteDescriptors.put(type, descriptor);
                descriptor.initializeFields(buildFieldDescriptors(beanDescription));

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

    private boolean isMetadataAware(BasicBeanDescription beanDescription) {
        SalesforceObject annotation = beanDescription.getClassAnnotations().get(SalesforceObject.class);
        return (annotation != null) && annotation.metadataAware();
    }

    private List<FieldDescriptor> buildFieldDescriptors(BasicBeanDescription beanDescription) {
        List<FieldDescriptor> fields = new ArrayList<FieldDescriptor>();
        for (BeanPropertyDefinition property : beanDescription.findProperties()) {
            Class<?> type = getConcreteClass(beanDescription, property);
            ObjectDescriptor relatedObject = getRelatedObject(type);
            List<ObjectDescriptor> polymorphicChoices = getPolymorphicChoices(property, type);
            fields.add(new FieldDescriptor(property.getName(), property.getAccessor(), type, relatedObject, polymorphicChoices));
        }
        return fields;
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

    private ObjectDescriptor getRelatedObject(Class<?> type) {
        return getObjectDescriptor(type);
    }

    private boolean isPolymorphic(BeanPropertyDefinition property) {
        SerializationConfig config = objectMapper.getSerializationConfig();
        return config.getAnnotationIntrospector().findPropertyTypeResolver(config, getAnnotatedMember(property), null) != null;
    }

    private Collection<NamedType> getSubtypes(BeanPropertyDefinition property) {
        SerializationConfig config = objectMapper.getSerializationConfig();
        return config.getSubtypeResolver().collectAndResolveSubtypes(
            getAnnotatedMember(property), config, config.getAnnotationIntrospector(), null);
    }

    private static AnnotatedMember getAnnotatedMember(BeanPropertyDefinition property) {
        return property.getAccessor() != null ? property.getAccessor() : property.getMutator();
    }
}
