/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;
import java.util.List;

/**
 * Extra metadata about a Salesforce Field above and beyond that normally managed by Jackson. The information is
 * collected at the same time as the core Jackson metadata (introspection time) and references some of the standard
 * Jackson classes.
 */
public final class FieldDescriptor {
    private final BeanPropertyDefinition property;
    private final Class<?> fieldClass;
    private final ObjectDescriptor relatedObject;
    private final List<ObjectDescriptor> polymorphicChoices;

    FieldDescriptor(BeanPropertyDefinition property, Class<?> fieldClass, ObjectDescriptor relatedObject, List<ObjectDescriptor> polymorphicChoices) {
        this.property = property;
        this.fieldClass = fieldClass;
        this.relatedObject = relatedObject;
        this.polymorphicChoices = polymorphicChoices;
    }

    public String getName() {
        return property.getName();
    }

    public BeanPropertyDefinition getProperty() {
        return property;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }

    public ObjectDescriptor getRelatedObject() {
        return relatedObject;
    }

    public List<ObjectDescriptor> getPolymorphicChoices() {
        return polymorphicChoices;
    }

    public boolean isArrayOrCollection() {
        return fieldClass.isArray() || Collection.class.isAssignableFrom(fieldClass);
    }

    public boolean isRelationship() {
        return relatedObject != null || isPolymorphic();
    }

    public boolean isPolymorphic() {
        return polymorphicChoices != null && polymorphicChoices.size() > 0;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
