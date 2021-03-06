/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

/**
 * Extra metadata about a Salesforce Field above and beyond that normally managed by Jackson. The information is
 * collected at the same time as the core Jackson metadata (introspection time) and references some of the standard
 * Jackson classes.
 */
public final class FieldDescriptor implements Serializable {

    private static final long serialVersionUID = 4291181727751149768L;

    private final String name;
    private final JavaType javaType;
    private final AnnotatedMember accessor;
    private final AnnotatedMember mutator;
    private final ObjectDescriptor relatedObject;
    private final List<ObjectDescriptor> polymorphicChoices;

    FieldDescriptor(String name, AnnotatedMember accessor, AnnotatedMember mutator, JavaType javaType, ObjectDescriptor relatedObject, List<ObjectDescriptor> polymorphicChoices) {
        this.name = name;
        this.accessor = accessor;
        this.mutator = mutator;
        this.javaType = javaType;
        this.relatedObject = relatedObject;
        this.polymorphicChoices = polymorphicChoices;
    }

    public String getName() {
        return name;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public ObjectDescriptor getRelatedObject() {
        return relatedObject;
    }

    public List<ObjectDescriptor> getPolymorphicChoices() {
        return polymorphicChoices;
    }

    public boolean isRelationship() {
        return relatedObject != null || isPolymorphic();
    }

    public boolean isPolymorphic() {
        return polymorphicChoices != null && polymorphicChoices.size() > 0;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Object record) {
        if (accessor != null) {
            return (T) accessor.getValue(record);
        } else {
            throw new IllegalStateException("Field has no accessor");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void setValue(Object record, T value) {
        if (mutator != null) {
            mutator.setValue(record, value);
        } else {
            throw new IllegalStateException("Field has no mutator");
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
