/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

/**
 * Extra metadata about a Salesforce Field above and beyond that normally managed by Jackson. The information is
 * collected at the same time as the core Jackson metadata (introspection time) and references some of the standard
 * Jackson classes.
 */
public class FieldDescriptor {
    private final BeanPropertyDefinition property;

    FieldDescriptor(BeanPropertyDefinition property) {
        this.property = property;
    }

    public BeanPropertyDefinition getProperty() {
        return property;
    }

    public String getName() {
        return property.getName();
    }
}
