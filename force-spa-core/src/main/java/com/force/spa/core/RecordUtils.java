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
     * Gets the value of the "attributes" property of a record.
     *
     * @param descriptor descriptor of the object
     * @param record     the record from which to get the attributes
     * @return the attributes
     */
    public static Map<String, String> getAttributes(ObjectDescriptor descriptor, Object record) {
        if (descriptor.hasAttributesMember()) {
            return getAttributes(descriptor.getAttributesProperty(), record);
        } else {
            throw new IllegalArgumentException("The record does not have an attributes member");
        }
    }

    /**
     * Gets the value of the "attributes" property of a record.
     *
     * @param attributesProperty definition of the attributes property
     * @param record             the record from which to get the attributes
     * @return the attributes
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getAttributes(BeanPropertyDefinition attributesProperty, Object record) {
        try {
            Object attributes;
            if (attributesProperty.hasGetter()) {
                attributes = attributesProperty.getGetter().getAnnotated().invoke(record);
            } else if (attributesProperty.hasField()) {
                Field field = attributesProperty.getField().getAnnotated();
                field.setAccessible(true);
                attributes = field.get(record);
            } else {
                throw new IllegalStateException("There is no way to get the record attributes");
            }
            return (Map<String, String>) attributes;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the "id" property of a record.
     *
     * @param descriptor descriptor of the object
     * @param record     the record from which to get the id
     * @return the id
     */
    public static String getId(ObjectDescriptor descriptor, Object record) {
        if (descriptor.hasIdMember()) {
            return getId(descriptor.getIdProperty(), record);
        } else {
            throw new IllegalArgumentException("The record does not have an id member");
        }
    }

    /**
     * Gets the "id" property of a record.
     *
     * @param idProperty definition of the id property
     * @param record     the entity record from which to get the id
     * @return the id
     */
    public static String getId(BeanPropertyDefinition idProperty, Object record) {
        try {
            Object id;
            if (idProperty.hasGetter()) {
                id = idProperty.getGetter().getAnnotated().invoke(record);
            } else if (idProperty.hasField()) {
                Field field = idProperty.getField().getAnnotated();
                field.setAccessible(true);
                id = field.get(record);
            } else {
                throw new IllegalStateException("There is no way to get the record id");
            }
            return id == null ? null : id.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the "id" property of a record.
     *
     * @param descriptor descriptor of the object
     * @param record     the record on which to set the id
     * @param value      the id
     */
    public static void setId(ObjectDescriptor descriptor, Object record, String value) {
        if (descriptor.hasIdMember()) {
            setId(descriptor.getIdProperty(), record, value);
        } else {
            throw new IllegalArgumentException("The record does not have an id member");
        }
    }

    /**
     * Sets the "id" property of a record.
     *
     * @param idProperty definition of the id property
     * @param record     the record on which to set the id
     * @param value      the id
     */
    public static void setId(BeanPropertyDefinition idProperty, Object record, String value) {
        if (idProperty.hasSetter()) {
            idProperty.getSetter().setValue(record, value);
        } else if (idProperty.hasField()) {
            Field field = idProperty.getField().getAnnotated();
            field.setAccessible(true);
            idProperty.getField().setValue(record, value);
        } else
            throw new IllegalArgumentException("There is no way to set the record id");
    }
}
