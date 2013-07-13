/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;
import java.util.Collections;

/**
 * Extra metadata about a Salesforce Field above and beyond that normally managed by Jackson. The information is
 * collected at the same time as the core Jackson metadata (introspection time) and references some of the standard
 * Jackson classes.
 */
public class FieldDescriptor {
    private final BeanPropertyDefinition property;
    private final Collection<NamedType> subtypes;

    FieldDescriptor(BeanPropertyDefinition property, Collection<NamedType> subtypes) {
        this.property = property;
        this.subtypes = Collections.unmodifiableCollection(subtypes);
    }

    public BeanPropertyDefinition getProperty() {
        return property;
    }

    public String getName() {
        return property.getName();
    }

    public Collection<NamedType> getSubtypes() {
        return subtypes;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
