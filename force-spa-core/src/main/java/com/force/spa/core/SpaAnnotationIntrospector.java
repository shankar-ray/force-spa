/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import java.io.IOException;

/**
 * An {@link com.fasterxml.jackson.databind.AnnotationIntrospector} which understands the special persistence
 * annotations defined by this library (@link SalesforceObject} and {@link SalesforceField}. The annotations provide
 * basic information for serializing and deserializing (the usual Jackson stuff) and also provide relationship
 * information which is used elsewhere to help build SOQL queries to fetch trees of objects.
 * <p/>
 * This implementation also accepts a limited subset of JPA annotations for backward compatability with earlier library
 * versions. Eventually that support may go away.
 *
 * @see SalesforceObject
 * @see SalesforceField
 */
class SpaAnnotationIntrospector extends NopAnnotationIntrospector {

    private final transient ObjectMappingContext mappingContext;

    private static final Class<?>[] NEVER_VIEWS = new Class<?>[]{SerializationViews.Never.class};
    private static final Class<?>[] CREATE_VIEWS = new Class<?>[]{SerializationViews.Create.class};
    private static final Class<?>[] UPDATE_VIEWS = new Class<?>[]{SerializationViews.Update.class, SerializationViews.Patch.class};

    SpaAnnotationIntrospector(ObjectMappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    @Override
    public String findTypeName(AnnotatedClass annotatedClass) {
        return findSalesforceObjectName(annotatedClass);
    }

    @Override
    public PropertyName findRootName(AnnotatedClass annotatedClass) {
        String simpleName = findSalesforceObjectName(annotatedClass);
        return (simpleName == null) ? null : new PropertyName(simpleName);
    }

    @Override
    public PropertyName findNameForSerialization(Annotated annotated) {
        String simpleName = findSalesforceFieldName(annotated);
        return (simpleName == null) ? null : new PropertyName(simpleName);
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated annotated) {
        String simpleName = findSalesforceFieldName(annotated);
        return (simpleName == null) ? null : new PropertyName(simpleName);
    }

    @Override
    public Class<?>[] findViews(Annotated annotated) {
        boolean insertable = isInsertable(annotated);
        boolean updatable = isUpdatable(annotated);

        if (!insertable || !updatable) {
            if (!insertable && !updatable) {
                return NEVER_VIEWS;   // Never serialized
            } else if (insertable) {
                return CREATE_VIEWS;  // Only serialized for create
            } else {
                return UPDATE_VIEWS;  // Only serialized for update (and patch).
            }
        } else {
            return super.findViews(annotated);
        }
    }

    @Override
    public Boolean findIgnoreUnknownProperties(AnnotatedClass ac) {
        return true;
    }

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember member) {
        return member.getAnnotation(Transient.class) != null;
    }

    @Override
    public String findEnumValue(Enum<?> value) {
        return value.toString(); // Use the "pretty" toString value
    }

    @Override
    public Object findSerializer(Annotated annotated) {
        final ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(annotated.getRawType());
        if (IntrospectionUtils.isChildToParentRelationship(annotated) && descriptor != null) {
            // Return a member-specific serializer that serializes just the entity's id instead of the whole entity.
            // For serialization of relationships (headed to database.com) we just serialize the id. This is important
            // to achieve the desired semantic for relating existing objects through the Salesforce REST API.
            return new JsonSerializer<Object>() {
                @Override
                public void serialize(Object object, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                    jgen.writeString(RecordUtils.getId(descriptor, object));
                }
            };
        }
        return super.findSerializer(annotated);
    }

    private static String findSalesforceObjectName(AnnotatedClass annotatedClass) {
        SalesforceObject salesforceObject = annotatedClass.getAnnotation(SalesforceObject.class);
        if (salesforceObject != null && StringUtils.isNotEmpty(salesforceObject.name())) {
            return salesforceObject.name();
        }

        Entity entity = annotatedClass.getAnnotation(Entity.class);
        if (entity != null && StringUtils.isNotEmpty(entity.name())) {
            return entity.name();
        }

        return null;
    }

    private static String findSalesforceFieldName(Annotated annotated) {
        SalesforceField salesforceField = annotated.getAnnotation(SalesforceField.class);
        if (salesforceField != null && StringUtils.isNotEmpty(salesforceField.name())) {
            return salesforceField.name();
        }

        Column column = annotated.getAnnotation(Column.class);
        if (column != null && StringUtils.isNotEmpty(column.name())) {
            return column.name();
        }

        JoinColumn joinColumn = annotated.getAnnotation(JoinColumn.class);
        if (joinColumn != null && StringUtils.isNotEmpty(joinColumn.name())) {
            return joinColumn.name();
        }

        return null;
    }

    private static boolean isInsertable(Annotated annotated) {
        if (IntrospectionUtils.isNonInsertableStandardProperty(getPropertyName(annotated))) {
            return false;
        }

        SalesforceField salesforceField = annotated.getAnnotation(SalesforceField.class);
        if (salesforceField != null && !salesforceField.insertable()) {
            return false;
        }

        Column column = annotated.getAnnotation(Column.class);
        if (column != null && !column.insertable()) {
            return false;
        }

        JoinColumn joinColumn = annotated.getAnnotation(JoinColumn.class);
        if (joinColumn != null && !joinColumn.insertable()) {
            return false;
        }

        return true;
    }

    private boolean isUpdatable(Annotated annotated) {
        if (IntrospectionUtils.isNonUpdatableStandardProperty(getPropertyName(annotated))) {
            return false;
        }

        SalesforceField salesforceField = annotated.getAnnotation(SalesforceField.class);
        if (salesforceField != null && !salesforceField.updatable()) {
            return false;
        }

        Column column = annotated.getAnnotation(Column.class);
        if (column != null && !column.updatable()) {
            return false;
        }

        JoinColumn joinColumn = annotated.getAnnotation(JoinColumn.class);
        if (joinColumn != null && !joinColumn.updatable()) {
            return false;
        }

        return true;
    }

    private static String getPropertyName(Annotated annotated) {
        if (annotated instanceof AnnotatedField) {

            AnnotatedField field = (AnnotatedField) annotated;
            String name = findSalesforceFieldName(annotated);
            return (name != null) ? name : field.getName();

        } else if (annotated instanceof AnnotatedMethod) {

            AnnotatedMethod method = (AnnotatedMethod) annotated;
            String name = findSalesforceFieldName(method);
            if (name == null) {
                if (method.getParameterCount() == 0) {
                    name = BeanUtil.okNameForGetter(method);
                } else {
                    name = BeanUtil.okNameForSetter(method);
                }
                if (name == null) {
                    throw new IllegalStateException("Unable to figure out the property name");
                }
            }
            return name;

        } else {
            throw new IllegalArgumentException("Unrecognized instance of 'Annotated'");
        }
    }
}
