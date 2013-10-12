/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import static com.force.spa.core.IntrospectionUtils.canBeSalesforceObject;
import static com.force.spa.core.utils.JavaTypeUtils.getJavaTypeFor;

import java.io.InputStream;
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
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
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
import com.force.spa.RecordResponseException;
import com.force.spa.SalesforceObject;
import com.force.spa.core.utils.CountingJsonFactory;
import com.force.spa.core.utils.CountingJsonParser;

/**
 * Context for mapping annotated persistent objects to and from the JSON representations of the Salesforce generic REST
 * API.
 * <p/>
 * This context includes the basic Jackson {@link ObjectMapper} configured in addition to extra metadata in the form of
 * {@link ObjectDescriptor}s for making advanced choices related to Salesforce persistence.
 */
public final class MappingContext implements Serializable {

    private static final long serialVersionUID = 7444706031630632301L;

    private final RecordAccessorConfig config;
    private final ObjectMapper objectMapper;
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriterForCreate;
    private final ObjectWriter objectWriterForUpdate;
    private final ObjectWriter objectWriterForPatch;

    // Updates happen infrequently once the context is primed so no need for special concurrent map parameters.
    private final Map<String, ObjectDescriptor> descriptorsByName = new ConcurrentHashMap<>();
    private final Map<Class<?>, ObjectDescriptor> descriptorsByClass = new ConcurrentHashMap<>();

    private final Map<Class<?>, ObjectDescriptor> descriptorsUnderConstruction = new HashMap<>();
    private final Set<Class<?>> rejectedClasses = new HashSet<>();

    public MappingContext() {
        this(new RecordAccessorConfig());
    }

    public MappingContext(RecordAccessorConfig config) {
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

        JsonFactory countingJsonFactory = new CountingJsonFactory();
        DefaultDeserializationContext deserializationContext = new DefaultDeserializationContext.Impl(new SubqueryDeserializerFactory());
        ObjectMapper objectMapper = new ObjectMapper(countingJsonFactory, null, deserializationContext);

        countingJsonFactory.setCodec(objectMapper);

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

    public CountingJsonParser createParser(InputStream inputStream) {
        try {
            JsonParser parser = getObjectReader().getFactory().createParser(inputStream);
            if (parser instanceof CountingJsonParser) {
                return (CountingJsonParser) parser;
            } else {
                throw new IllegalStateException("The JsonFactory is not configuration properly to return CountingJsonParsers");
            }
        } catch (Exception e) {
            throw new RecordResponseException("Failed to create JSON parser", e);
        }
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
     *
     * @return the descriptor
     */
    public ObjectDescriptor getObjectDescriptor(Class<?> type) {
        ObjectDescriptor descriptor = findObjectDescriptor(type);
        if (descriptor == null) {
            throw new IllegalArgumentException("Unable to build an object descriptor for " + type.getName());
        }
        return descriptor;
    }

    /**
     * Finds the {@link ObjectDescriptor} for the specified class.
     * <p/>
     * If the class has already been seen then an existing, cached descriptor is returned. Otherwise the class is
     * introspected to build a new descriptor and the new descriptor cached for future use.
     *
     * @param type the class for which a descriptor is desired
     *
     * @return the descriptor or null if none applies to the object
     */
    public ObjectDescriptor findObjectDescriptor(Class<?> type) {
        ObjectDescriptor descriptor = descriptorsByClass.get(type);
        if (descriptor != null) {
            return descriptor;
        }

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
     * Finds the {@link ObjectDescriptor} with the specified name.
     *
     * @param name the name of the object for which a descriptor is desired
     *
     * @return the descriptor or null if not found
     */
    public ObjectDescriptor findObjectDescriptor(String name) {
        return (name != null) ? descriptorsByName.get(name) : null;
    }

    /**
     * Create an object descriptor (and all of its related descriptors).
     * <p/>
     * We do a little extra work here to handle forward and backward recursive references. We can return partially
     * constructed entries while recursing (in order to avoid duplicate entries). But don't worry, by the time the
     * recursive call chain unravels all the descriptors will be fully initialized.
     */
    private ObjectDescriptor createObjectDescriptor(Class<?> type) {
        synchronized (descriptorsUnderConstruction) { // Just one thread can create at a time. Creation doesn't happen often.

            if (descriptorsUnderConstruction.containsKey(type)) {
                return descriptorsUnderConstruction.get(type);  // Return existing entry rather than create a duplicate
            }

            boolean recursiveCall = isRecursiveCall();
            try {
                BasicBeanDescription beanDescription = getBeanDescription(type);
                ObjectDescriptor descriptor = buildObjectDescriptor(beanDescription);

                descriptorsUnderConstruction.put(type, descriptor); // Stash partial progress before handling fields that might recurse

                descriptor.initializeFields(buildFieldDescriptors(beanDescription));

                if (!recursiveCall) {
                    publishCompletedDescriptors(descriptorsUnderConstruction);
                }

                return descriptor;

            } finally {
                if (!recursiveCall) {
                    descriptorsUnderConstruction.clear();
                }
            }
        }
    }

    private boolean isRecursiveCall() {
        return descriptorsUnderConstruction.size() > 0;
    }

    private BasicBeanDescription getBeanDescription(Class<?> type) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(type);
        return objectMapper.getSerializationConfig().introspect(javaType);
    }

    private ObjectDescriptor buildObjectDescriptor(BasicBeanDescription bean) {
        return new ObjectDescriptor(findObjectName(bean), bean.getType(), isMetadataAware(bean));
    }

    private String findObjectName(BasicBeanDescription bean) {
        AnnotationIntrospector introspector = objectMapper.getDeserializationConfig().getAnnotationIntrospector();
        String name = introspector.findTypeName(bean.getClassInfo());
        if (!StringUtils.isEmpty(name))
            return name;

        return bean.getClassInfo().getRawType().getSimpleName();
    }

    private boolean isMetadataAware(BasicBeanDescription bean) {
        SalesforceObject annotation = bean.getClassAnnotations().get(SalesforceObject.class);
        return (annotation != null) && annotation.metadataAware();
    }

    private List<FieldDescriptor> buildFieldDescriptors(BasicBeanDescription bean) {
        List<FieldDescriptor> fields = new ArrayList<>();
        for (BeanPropertyDefinition property : bean.findProperties()) {
            fields.add(buildFieldDescriptor(bean, property));
        }
        return fields;
    }

    private FieldDescriptor buildFieldDescriptor(BasicBeanDescription bean, BeanPropertyDefinition property) {
        JavaType javaType = getJavaTypeFor(property, bean);
        ObjectDescriptor baseRelatedObject = findRelatedObject(javaType);
        List<ObjectDescriptor> polymorphicChoices = getPolymorphicChoices(property, baseRelatedObject);
        return new FieldDescriptor(property.getName(), property.getAccessor(), property.getMutator(), javaType, baseRelatedObject, polymorphicChoices);
    }

    private ObjectDescriptor findRelatedObject(JavaType javaType) {
        if (javaType.isContainerType()) {
            return findRelatedObject(javaType.containedType(0));
        } else {
            return findObjectDescriptor(javaType.getRawClass());
        }
    }

    private List<ObjectDescriptor> getPolymorphicChoices(BeanPropertyDefinition property, ObjectDescriptor baseObject) {
        if (baseObject != null) {
            JavaType baseJavaType = baseObject.getJavaType();
            List<ObjectDescriptor> polymorphicChoices = new ArrayList<>();
            for (NamedType subtype : getSubtypes(property, baseJavaType)) {
                if (!baseJavaType.getRawClass().equals(subtype.getType())) {
                    polymorphicChoices.add(getObjectDescriptor(subtype.getType()));
                }
            }

            if (polymorphicChoices.size() > 0) {
                return Collections.unmodifiableList(polymorphicChoices);
            }
        }
        return Collections.emptyList();
    }

    private Collection<NamedType> getSubtypes(BeanPropertyDefinition property, JavaType baseType) {
        SerializationConfig config = objectMapper.getSerializationConfig();
        return config.getSubtypeResolver().collectAndResolveSubtypes(
            getAnnotatedMember(property), config, config.getAnnotationIntrospector(), baseType);
    }

    private static AnnotatedMember getAnnotatedMember(BeanPropertyDefinition property) {
        return property.getAccessor() != null ? property.getAccessor() : property.getMutator();
    }

    private void publishCompletedDescriptors(Map<Class<?>, ObjectDescriptor> descriptors) {
        for (Map.Entry<Class<?>, ObjectDescriptor> entry : descriptors.entrySet()) {
            ObjectDescriptor descriptor = entry.getValue();
            descriptorsByClass.put(entry.getKey(), descriptor);
            descriptorsByName.put(descriptor.getName(), descriptor);
        }
    }
}
