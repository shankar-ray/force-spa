/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;


import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extra metadata about a Salesforce Object above and beyond that normally managed by Jackson. The information is
 * collected at the same time as the core Jackson metadata (introspection time) and references some of the standard
 * Jackson classes.
 */
public final class ObjectDescriptor {
    private static final String ID_FIELD = "Id";
    private static final String ATTRIBUTES_FIELD = "attributes";

    private final String name;
    private final List<FieldDescriptor> fields;
    private final Map<String, FieldDescriptor> fieldsByName;
    private final Map<String, ObjectDescriptor> relatedObjects;

    ObjectDescriptor(String name, List<FieldDescriptor> fields) {
        this.name = name;
        this.relatedObjects = new HashMap<String, ObjectDescriptor>();
        this.fields = Collections.unmodifiableList(fields);

        this.fieldsByName = new HashMap<String, FieldDescriptor>();
        for (FieldDescriptor field : fields) {
            fieldsByName.put(field.getName(), field);
        }
    }

    public boolean hasField(String name) {
        return fieldsByName.get(name) != null;
    }

    public boolean hasAttributesField() {
        return hasField(ATTRIBUTES_FIELD);
    }

    public boolean hasIdField() {
        return hasField(ID_FIELD);
    }

    public FieldDescriptor getField(String name) {
        FieldDescriptor descriptor = fieldsByName.get(name);
        if (descriptor == null) {
            throw new IllegalStateException(String.format("The object does not have an \'%s\' field", name));
        }
        return descriptor;
    }

    public FieldDescriptor getIdField() {
        return getField(ID_FIELD);
    }

    public FieldDescriptor getAttributesField() {
        return getField(ATTRIBUTES_FIELD);
    }

    public String getName() {
        return name;
    }

    public List<FieldDescriptor> getFields() {
        return fields;
    }

    public Map<String, ObjectDescriptor> getRelatedObjects() {
        return relatedObjects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(name)
            .append(fields)
            .append(relatedObjects)
            .build();
    }
}
