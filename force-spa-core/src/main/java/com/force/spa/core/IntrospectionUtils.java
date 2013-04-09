/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Miscellaneous utilities for asking questions about objects and their metadata.
 */
final class IntrospectionUtils {
    // The names of standard Salesforce properties.
    private static final Set<String> STANDARD_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            "Id", "Name", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate", "Owner",
            "MasterLabel", "DeveloperName", "Language", "RecordType", "attributes")));

    // The names of standard Salesforce properties that can not be sent with record creation.
    private static final Set<String> NON_INSERTABLE_STANDARD_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            "Id", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate", "attributes")));

    // The names of standard Salesforce properties that can not be sent with record update.
    private static final Set<String> NON_UPDATABLE_STANDARD_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            "Id", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate", "attributes")));

    private IntrospectionUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    static boolean isPropertyOfCustomEntity(AnnotatedMember member) {
        return getObjectName(member.getDeclaringClass()).endsWith("__c");
    }

    static boolean isStandardProperty(String name) {
        return STANDARD_PROPERTIES.contains(name);
    }

    static boolean isNonInsertableStandardProperty(String name) {
        return NON_INSERTABLE_STANDARD_PROPERTIES.contains(name);
    }

    static boolean isNonUpdatableStandardProperty(String name) {
        return NON_UPDATABLE_STANDARD_PROPERTIES.contains(name);
    }

    static boolean isChildToParentRelationship(Annotated annotated) {
        if (!(annotated instanceof AnnotatedMember))
            return false;

        AnnotatedMember annotatedMember = (AnnotatedMember) annotated;
        if (hasChildToParentAnnotation(annotatedMember))
            return true;

        Field relatedField = getRelatedField(annotatedMember);
        if (relatedField != null && hasChildToParentAnnotation(relatedField))
            return true;

        return false;
    }

    private static String getObjectName(Class<?> clazz) {
        SalesforceObject annotation = clazz.getAnnotation(SalesforceObject.class);
        if (annotation != null) {
            return annotation.name();
        }

        Entity annotation2 = clazz.getAnnotation(Entity.class);
        if (annotation2 != null) {
            return annotation2.name();
        }

        return clazz.getSimpleName();
    }

    private static boolean hasChildToParentAnnotation(Annotated annotated) {
        return annotated.hasAnnotation(ChildToParent.class)
            || annotated.hasAnnotation(ManyToOne.class)
            || annotated.hasAnnotation(OneToOne.class);
    }

    private static boolean hasChildToParentAnnotation(Field field) {
        return field.isAnnotationPresent(ChildToParent.class)
            || field.isAnnotationPresent(ManyToOne.class)
            || field.isAnnotationPresent(OneToOne.class);
    }

    /**
     * Find the {@link Field} that corresponds to the annotated member. When the annotated member is a setter or getter
     * this is used to find the corresponding field definition.
     *
     * @param member an annotated member
     * @return a field that corresponds to the annotated member
     */
    private static Field getRelatedField(AnnotatedMember member) {
        if (member instanceof AnnotatedField)
            return ((AnnotatedField) member).getAnnotated();

        String methodName = member.getName();
        if (!(methodName.startsWith("get") || methodName.startsWith("set")))
            return null;

        String relatedFieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        try {
            return member.getDeclaringClass().getDeclaredField(relatedFieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
