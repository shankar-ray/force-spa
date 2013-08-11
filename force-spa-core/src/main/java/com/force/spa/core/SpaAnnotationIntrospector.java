/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.force.spa.Polymorphic;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

import static com.force.spa.core.IntrospectionUtils.getRelatedElements;

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

    private static final long serialVersionUID = -6877076662700362622L;

    private static final String[] STARTING_PROPERTY_ORDER = new String[]{"attributes", "Id", "Name"};

    private static final Class<?>[] NEVER_VIEWS = new Class<?>[]{SerializationViews.Never.class};
    private static final Class<?>[] CREATE_VIEWS = new Class<?>[]{SerializationViews.Create.class};
    private static final Class<?>[] UPDATE_VIEWS = new Class<?>[]{SerializationViews.Update.class, SerializationViews.Patch.class};

    private final boolean auditFieldWritingAllowed;
    private final ObjectMappingContext mappingContext;

    SpaAnnotationIntrospector(ObjectMappingContext mappingContext, RecordAccessorConfig config) {
        this.auditFieldWritingAllowed = config.isAuditFieldWritingAllowed();
        this.mappingContext = mappingContext;
    }

    @Override
    public String findTypeName(AnnotatedClass annotatedClass) {
        return findSpecifiedSalesforceObjectName(annotatedClass.getAnnotated());
    }

    @Override
    public PropertyName findRootName(AnnotatedClass annotatedClass) {
        String simpleName = findSpecifiedSalesforceObjectName(annotatedClass.getAnnotated());
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
    public Boolean isTypeId(AnnotatedMember member) {
        return member.hasAnnotation(JsonTypeId.class);
    }

    @Override
    public List<NamedType> findSubtypes(Annotated annotated) {
        Polymorphic polymorphic = annotated.getAnnotation(Polymorphic.class);
        if (polymorphic == null || polymorphic.value() == null || polymorphic.value().length == 0) {
            return null;
        }

        ArrayList<NamedType> result = new ArrayList<NamedType>();
        for (Class<?> type : polymorphic.value()) {
            result.add(new NamedType(type, findSalesforceObjectName(type)));
        }
        return result;
    }

    @Override
    public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
        return findTypeResolver(config, (Annotated) ac, baseType);
    }

    @Override
    public TypeResolverBuilder<?> findPropertyTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType baseType) {
        return findTypeResolver(config, am, baseType);
    }

    @Override
    public TypeResolverBuilder<?> findPropertyContentTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType containerType) {
        return findTypeResolver(config, am, containerType);
    }

    @Override
    public String[] findSerializationPropertyOrder(AnnotatedClass ac) {
        return STARTING_PROPERTY_ORDER; // The order for a few fields we want at the front (if they exist).
    }

    @Override
    public Object findSerializer(Annotated annotated) {
        if ("attributes".equals(findSalesforceFieldName(annotated))) {
            return new AttributesSerializer();
        } else {
            return super.findSerializer(annotated);
        }
    }

    @SuppressWarnings("UnusedParameters")
    private TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, Annotated annotated, JavaType baseType) {
        Polymorphic polymorphic = annotated.getAnnotation(Polymorphic.class);
        if (polymorphic == null || polymorphic.value() == null || polymorphic.value().length == 0) {
            return null;
        }

        return new SpaTypeResolverBuilder(mappingContext).init(JsonTypeInfo.Id.NAME, null);
    }

    private static String findSpecifiedSalesforceObjectName(Class<?> clazz) {
        SalesforceObject salesforceObject = clazz.getAnnotation(SalesforceObject.class);
        if (salesforceObject != null) {
            return salesforceObject.name();
        }
        Entity entity = clazz.getAnnotation(Entity.class);
        if (entity != null) {
            return entity.name();
        }
        return null;
    }

    private static String findSalesforceObjectName(Class<?> clazz) {
        String name = findSpecifiedSalesforceObjectName(clazz);
        if (name == null) {
            name = clazz.getSimpleName();
        }
        return name;
    }

    private static String findSalesforceFieldName(Annotated annotated) {
        if (!(annotated instanceof AnnotatedField || annotated instanceof AnnotatedMethod)) {
            return null;
        }

        // Search the field, getter, and setter for relevant annotations.
        for (AnnotatedElement element : getRelatedElements((AnnotatedMember) annotated)) {
            SalesforceField salesforceField = element.getAnnotation(SalesforceField.class);
            if (salesforceField != null) {
                return salesforceField.name();
            }

            Column column = element.getAnnotation(Column.class);
            if (column != null) {
                return column.name();
            }

            JoinColumn joinColumn = element.getAnnotation(JoinColumn.class);
            if (joinColumn != null) {
                return joinColumn.name();
            }
        }
        return null;
    }

    private boolean isInsertable(Annotated annotated) {
        if (IntrospectionUtils.isNonInsertableStandardProperty(getPropertyName(annotated), auditFieldWritingAllowed)) {
            return false;
        }

        // Search the field, getter, and setter for relevant annotations.
        for (AnnotatedElement element : getRelatedElements((AnnotatedMember) annotated)) {
            SalesforceField salesforceField = element.getAnnotation(SalesforceField.class);
            if (salesforceField != null) {
                return salesforceField.insertable();
            }

            Column column = element.getAnnotation(Column.class);
            if (column != null) {
                return column.insertable();
            }

            JoinColumn joinColumn = element.getAnnotation(JoinColumn.class);
            if (joinColumn != null) {
                return joinColumn.insertable();
            }
        }
        return true;
    }

    private boolean isUpdatable(Annotated annotated) {
        if (IntrospectionUtils.isNonUpdatableStandardProperty(getPropertyName(annotated), auditFieldWritingAllowed)) {
            return false;
        }

        // Search the field, getter, and setter for relevant annotations.
        for (AnnotatedElement element : getRelatedElements((AnnotatedMember) annotated)) {
            SalesforceField salesforceField = element.getAnnotation(SalesforceField.class);
            if (salesforceField != null) {
                return salesforceField.updatable();
            }

            Column column = element.getAnnotation(Column.class);
            if (column != null) {
                return column.updatable();
            }

            JoinColumn joinColumn = element.getAnnotation(JoinColumn.class);
            if (joinColumn != null) {
                return joinColumn.updatable();
            }
        }
        return true;
    }

    private static String getPropertyName(Annotated annotated) {
        String name = findSalesforceFieldName(annotated);
        if (name != null) {
            return name;
        } else if (annotated instanceof AnnotatedField) {
            return annotated.getName();
        } else if (annotated instanceof AnnotatedMethod) {
            AnnotatedMethod method = (AnnotatedMethod) annotated;
            if (method.getParameterCount() == 0) {
                return BeanUtil.okNameForGetter(method);
            } else {
                return BeanUtil.okNameForSetter(method);
            }
        } else {
            return null;
        }
    }
}
