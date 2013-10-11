/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.BeanUtil;

public final class JavaTypeUtils {

    private JavaTypeUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    public static JavaType getJavaTypeFor(BeanPropertyDefinition property, BasicBeanDescription bean) {
        return TypeFactory.defaultInstance().constructType(getGenericTypeFor(property), bean.getType());
    }

    public static JavaType getJavaTypeFor(AnnotatedMember member) {
        return TypeFactory.defaultInstance().constructType(getGenericTypeFor(member), member.getDeclaringClass());
    }

    private static Type getGenericTypeFor(BeanPropertyDefinition property) {
        if (property.hasSetter()) {
            return property.getSetter().getGenericParameterType(0);
        } else if (property.hasField()) {
            return property.getField().getGenericType();
        } else if (property.hasGetter()) {
            return property.getGetter().getGenericType();
        } else {
            throw new IllegalArgumentException("Something is wrong with the property definition");
        }
    }

    private static Type getGenericTypeFor(AnnotatedMember member) {
        if (member instanceof AnnotatedMethod) {
            AnnotatedMethod method = (AnnotatedMethod) member;
            if (BeanUtil.okNameForSetter(method) != null) {
                return method.getGenericParameterType(0);
            }
        }
        return member.getGenericType();
    }
}
