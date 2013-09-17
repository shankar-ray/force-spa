/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

/**
 * Extra metadata about a Salesforce Field above and beyond that normally managed by Jackson. The information is
 * collected at the same time as the core Jackson metadata (introspection time) and references some of the standard
 * Jackson classes.
 */
public final class FieldDescriptor implements Serializable {

    private static final long serialVersionUID = -7449364275029415469L;

    private final String name;
    private final Class<?> type;
    private final AnnotatedMember accessor;
    private final ObjectDescriptor relatedObject;
    private final List<ObjectDescriptor> polymorphicChoices;

    FieldDescriptor(String name, AnnotatedMember accessor, Class<?> type, ObjectDescriptor relatedObject, List<ObjectDescriptor> polymorphicChoices) {
        this.name = name;
        this.accessor = accessor;
        this.type = type;
        this.relatedObject = relatedObject;
        this.polymorphicChoices = polymorphicChoices;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public ObjectDescriptor getRelatedObject() {
        return relatedObject;
    }

    public List<ObjectDescriptor> getPolymorphicChoices() {
        return polymorphicChoices;
    }

    public boolean isArrayOrCollection() {
        return type.isArray() || Collection.class.isAssignableFrom(type);
    }

    public boolean isRelationship() {
        return relatedObject != null || isPolymorphic();
    }

    public boolean isPolymorphic() {
        return polymorphicChoices != null && polymorphicChoices.size() > 0;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Object record) {
       return (T) ((accessor != null) ? accessor.getValue(record) : null);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
