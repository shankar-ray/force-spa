/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.force.spa.SalesforceObject;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    /**
     * The names of standard Salesforce properties that can not be sent with record creation.
     */
    private static final Set<String> NON_INSERTABLE_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            "Id", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate")));

    /**
     * The names of standard Salesforce properties that can not be sent with record creation when "CreateAuditFields"
     * per is enabled in the org.
     */
    private static final Set<String> NON_INSERTABLE_PROPERTIES_AUDIT_OK = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            "Id")));

    /**
     * The names of standard Salesforce properties that can not be sent with record update.
     */
    private static final Set<String> NON_UPDATABLE_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            "Id", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate")));

    /**
     * The names of standard Salesforce properties that can not be sent with record update when "CreateAuditFields" per
     * is enabled in the org.
     */
    private static final Set<String> NON_UPDATABLE_PROPERTIES_AUDIT_OK = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            "Id")));

    private IntrospectionUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    static boolean isStandardProperty(String name) {
        return STANDARD_PROPERTIES.contains(name);
    }

    static boolean isNonInsertableStandardProperty(String name, boolean auditFieldWritingAllowed) {
        if (auditFieldWritingAllowed) {
            return NON_INSERTABLE_PROPERTIES_AUDIT_OK.contains(name);
        } else {
            return NON_INSERTABLE_PROPERTIES.contains(name);
        }
    }

    static boolean isNonUpdatableStandardProperty(String name, boolean auditFieldWritingAllowed) {
        if (auditFieldWritingAllowed) {
            return NON_UPDATABLE_PROPERTIES_AUDIT_OK.contains(name);
        } else {
            return NON_UPDATABLE_PROPERTIES.contains(name);
        }
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

    static boolean canBeSalesforceObject(Class<?> type, boolean objectAnnotationRequired) {
        if (type.isPrimitive() || isIntrinsicJavaPackage(type.getPackage()) || isJodaTimePackage(type.getPackage())) {
            return false;
        }

        if (isEnum(type)) {
            return false;
        }

        return hasSalesforceObjectAnnotation(type) || !objectAnnotationRequired;
    }

    /**
     * Find the concrete {@link Class} of a property taking into account parameterized types and generic variables. If
     * the property is an array or collection then the returned type is the type of contained elements.
     */
    static Class<?> getConcreteClass(BasicBeanDescription beanDescription, BeanPropertyDefinition property) {
        Class<?> beanClass = beanDescription.getType().getRawClass();
        if (property.hasSetter()) {
            return getConcreteClass(beanClass, property.getSetter().getGenericParameterType(0));
        } else if (property.hasField()) {
            return getConcreteClass(beanClass, property.getField().getGenericType());
        } else if (property.hasGetter()) {
            return getConcreteClass(beanClass, property.getGetter().getGenericType());
        } else {
            throw new IllegalArgumentException("I don't know how to deal with that kind of property");
        }
    }

    /**
     * Find the concrete {@link Class} of a member taking into account parameterized types and generic variables. If the
     * property is an array or collection then the returned type is the type of contained elements.
     */
    static Class<?> getConcreteClass(AnnotatedMember member) {
        Class<?> beanClass = member.getDeclaringClass();
        if (member instanceof AnnotatedMethod) {
            AnnotatedMethod method = (AnnotatedMethod) member;
            if (BeanUtil.okNameForSetter(method) != null) {
                return getConcreteClass(beanClass, method.getGenericParameterType(0));
            }
        }
        return getConcreteClass(beanClass, member.getGenericType());
    }

    private static Class<?> getConcreteClass(Class<?> beanClass, Type propertyType) {
        if (propertyType instanceof Class) {
            return getConcreteClassForClass((Class<?>) propertyType);

        } else if (propertyType instanceof ParameterizedType) {
            return getConcreteClassForParameterizedType(beanClass, (ParameterizedType) propertyType);

        } else if (propertyType instanceof TypeVariable) {
            return getConcreteClassForGenericVariable(beanClass, (TypeVariable) propertyType);

        } else {
            throw new IllegalArgumentException("I don't know how to deal with that kind of property");
        }
    }

    private static Class<?> getConcreteClassForClass(Class<?> clazz) {
        if (clazz.isArray()) {
            return clazz.getComponentType(); // Return element class
        } else {
            return clazz;
        }
    }

    private static Class<?> getConcreteClassForParameterizedType(Class<?> beanClass, ParameterizedType parameterizedType) {
        if (Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            return getConcreteClass(beanClass, parameterizedType.getActualTypeArguments()[0]); // Return element class
        } else {
            return getConcreteClass(beanClass, parameterizedType.getRawType());
        }
    }

    private static Class<?> getConcreteClassForGenericVariable(Class<?> beanClass, TypeVariable typeVariable) {
        Class<?> genericClass = (Class<?>) typeVariable.getGenericDeclaration();
        if (genericClass.isAssignableFrom(beanClass)) {
            JavaType[] boundTypes = TypeFactory.defaultInstance().findTypeParameters(beanClass, genericClass);
            TypeVariable[] typeVariables = genericClass.getTypeParameters();
            for (int i = 0; i < typeVariables.length; i++) {
                if (typeVariables[i].equals(typeVariable)) {
                    return boundTypes != null ? boundTypes[i].getRawClass() : genericClass;
                }
            }
        }
        throw new RuntimeException(
            String.format(
                "Don't know how to map type variable to a concrete type: variable=%s, beanClass=%s, genericClass=%s",
                typeVariable.getName(), beanClass.getSimpleName(), genericClass.getSimpleName()));
    }

    private static boolean hasSalesforceObjectAnnotation(Class<?> type) {
        return type.getAnnotation(SalesforceObject.class) != null;
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
    private static boolean isEnum(Class<?> type) {
        return ((type.getModifiers() & 0x4000)) != 0;
    }
}
