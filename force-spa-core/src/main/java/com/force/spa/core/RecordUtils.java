/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Utilities for working with record instances.
 */
public final class RecordUtils {
    private RecordUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    /**
     * Gets the value of the "id" field of a record.
     *
     * @param descriptor descriptor of the object
     * @param record     the record from which to get the id
     * @return the id
     */
    public static String getId(ObjectDescriptor descriptor, Object record) {
        return getFieldValue(descriptor.getIdField(), record);
    }

    /**
     * Gets the value of the "attributes" field of a record.
     *
     * @param descriptor descriptor of the object
     * @param record     the record from which to get the attributes
     * @return the attributes
     */
    public static Map<String, String> getAttributes(ObjectDescriptor descriptor, Object record) {
        return getFieldValue(descriptor.getAttributesField(), record);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getFieldValue(FieldDescriptor descriptor, Object record) {
        try {
            Object value;
            BeanPropertyDefinition property = descriptor.getProperty();
            if (property.hasGetter()) {
                value = property.getGetter().getAnnotated().invoke(record);
            } else if (property.hasField()) {
                Field field = property.getField().getAnnotated();
                field.setAccessible(true);
                value = field.get(record);
            } else {
                throw new IllegalStateException(String.format("There is no way to get the %s value", property.getName()));
            }
            return (T) value;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
