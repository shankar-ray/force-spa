/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;

/**
 * Utilities for working with Jackson.
 */
public final class JacksonUtils {

    private JacksonUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    /**
     * Find the {@link Class} of a property taking into account parameterized types and generic variables.
     */
    public static Class<?> getPropertyClass(BasicBeanDescription beanDescription, BeanPropertyDefinition property) {
        JavaType beanType = beanDescription.getType();
        if (property.hasSetter()) {
            return getPropertyClass(beanType, property.getSetter().getGenericParameterType(0));
        } else if (property.hasField()) {
            return getPropertyClass(beanType, property.getField().getGenericType());
        } else if (property.hasGetter()) {
            return getPropertyClass(beanType, property.getGetter().getGenericType());
        } else {
            throw new IllegalArgumentException(
                String.format("I don't know how to deal with that kind of property, beanType=%s, property=%s", beanType, property));
        }
    }

    /*
     * Gets the class of the related object. If the class of the property is an array or collection then it is the class
     * of the contained elements that is relevant. Otherwise the property class is the one we want.
     */
    public static Class<?> getPropertyClass(JavaType beanType, Type propertyType) {
        if (propertyType instanceof Class) {
            return getPropertyClassForClass((Class<?>) propertyType);

        } else if (propertyType instanceof ParameterizedType) {
            return getPropertyClassForParameterizedType(beanType, (ParameterizedType) propertyType);

        } else if (propertyType instanceof TypeVariable) {
            return getPropertyClassForGenericVariable(beanType, (TypeVariable) propertyType);

        } else {
            throw new IllegalArgumentException(
                String.format("I don't know how to deal with that kind of property, beanType=%s, propertyType=%s", beanType, propertyType));
        }
    }

    public static Class<?> getPropertyClassForClass(Class<?> clazz) {
        if (clazz.isArray()) {
            return clazz.getComponentType(); // Return element class
        } else {
            return clazz;
        }
    }

    public static Class<?> getPropertyClassForParameterizedType(JavaType beanType, ParameterizedType parameterizedType) {
        if (Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            return getPropertyClass(beanType, parameterizedType.getActualTypeArguments()[0]); // Return element class
        } else {
            return getPropertyClass(beanType, parameterizedType.getRawType());
        }
    }

    public static Class<?> getPropertyClassForGenericVariable(JavaType beanType, TypeVariable typeVariable) {
        Class<?> beanClass = beanType.getRawClass();
        Class<?> genericClass = (Class<?>) typeVariable.getGenericDeclaration();
        if (genericClass.isAssignableFrom(beanClass)) {
            JavaType[] boundTypes = TypeFactory.defaultInstance().findTypeParameters(beanClass, genericClass);
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
