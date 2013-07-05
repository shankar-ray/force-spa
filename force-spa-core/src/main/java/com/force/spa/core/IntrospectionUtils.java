/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.force.spa.ChildToParent;
import com.force.spa.SalesforceObject;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
            "Id", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate")));

    // The names of standard Salesforce properties that can not be sent with record update.
    private static final Set<String> NON_UPDATABLE_STANDARD_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            "Id", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate")));

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

    static boolean isChildToParentRelationship(AnnotatedMember member) {
        // Search the field, getter, and setter for relevant annotations. The given member is checked first.
        for (AnnotatedElement element : getRelatedElements(member)) {
            if (hasChildToParentAnnotation(element))
                return true;

        }
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

    private static boolean hasChildToParentAnnotation(AnnotatedElement element) {
        return element.isAnnotationPresent(ChildToParent.class)
            || element.isAnnotationPresent(ManyToOne.class)
            || element.isAnnotationPresent(OneToOne.class);
    }

    /**
     * Find all the members that are related to the given member. Members often come in threes (field, getter, and
     * setter). Given one, we often want to find the others so we can check certain annotations.
     * <p/>
     * If Jackson autodetect is turned on sometimes the member we are given will not be the one with the annotations.
     * This helper method allows us to find the others.
     */
    static List<AnnotatedElement> getRelatedElements(AnnotatedMember member) {

        String name = member.getName();
        if (member instanceof AnnotatedMethod) {
            AnnotatedMethod method = (AnnotatedMethod) member;
            if (method.getParameterCount() == 0) {
                name = BeanUtil.okNameForGetter(method);
            } else {
                name = BeanUtil.okNameForSetter(method);
            }
        }

        List<AnnotatedElement> relatedElements = new ArrayList<AnnotatedElement>();
        relatedElements.add(member.getAnnotated()); // The given element is always first in the list.

        if (name != null) {
            String nameWithInitialUpperCase = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            try {
                AnnotatedElement element = member.getDeclaringClass().getDeclaredField(name);
                if (!relatedElements.contains(element)) {
                    relatedElements.add(element);
                }
            } catch (NoSuchFieldException e) {
                // Nothing to add to list
            }

            try {
                String methodName = "get" + nameWithInitialUpperCase;
                AnnotatedElement element = member.getDeclaringClass().getDeclaredMethod(methodName);
                if (!relatedElements.contains(element)) {
                    relatedElements.add(element);
                }
            } catch (NoSuchMethodException e) {
                // Nothing to add to list
            }

            try {
                String methodName = "is" + nameWithInitialUpperCase;
                AnnotatedElement element = member.getDeclaringClass().getDeclaredMethod(methodName);
                if (!relatedElements.contains(element)) {
                    relatedElements.add(element);
                }
            } catch (NoSuchMethodException e) {
                // Nothing to add to list
            }

            try {
                String methodName = "set" + nameWithInitialUpperCase;
                AnnotatedElement element = member.getDeclaringClass().getDeclaredMethod(methodName, member.getRawType());
                if (!relatedElements.contains(element)) {
                    relatedElements.add(element);
                }
            } catch (NoSuchMethodException e) {
                // Nothing to add to list
            }
        }

        return relatedElements;
    }
}
