/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;


import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Extra metadata about a Salesforce Object above and beyond that normally managed by Jackson. The information is
 * collected at the same time as the core Jackson metadata (introspection time) and references some of the standard
 * Jackson classes.
 */
final class ObjectDescriptor {

    private final String name;
    private final BeanPropertyDefinition idProperty;
    private final BeanPropertyDefinition attributesProperty;
    private final BasicBeanDescription beanDescription;
    private final Map<String, ObjectDescriptor> relatedObjects;

    ObjectDescriptor(String name, BasicBeanDescription beanDescription, BeanPropertyDefinition idProperty, BeanPropertyDefinition attributesProperty) {
        this.name = name;
        this.beanDescription = beanDescription;
        this.idProperty = idProperty;
        this.attributesProperty = attributesProperty;
        this.relatedObjects = new HashMap<String, ObjectDescriptor>();
    }

    public String getName() {
        return name;
    }

    public BeanPropertyDefinition getIdProperty() {
        return idProperty;
    }

    public BeanPropertyDefinition getAttributesProperty() {
        return attributesProperty;
    }

    public boolean hasIdMember() {
        return getIdProperty() != null;
    }

    public boolean hasAttributesMember() {
        return getAttributesProperty() != null;
    }

    public BasicBeanDescription getBeanDescription() {
        return beanDescription;
    }

    public Map<String, ObjectDescriptor> getRelatedObjects() {
        return relatedObjects;
    }
}
