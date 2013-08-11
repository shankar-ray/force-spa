/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;


import com.force.spa.ObjectDefinitionException;
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
    public static final String ID_FIELD_NAME = "Id";
    public static final String ATTRIBUTES_FIELD_NAME = "attributes";

    private final String name;
    private List<FieldDescriptor> fields;
    private final Map<String, FieldDescriptor> fieldsByName;

    ObjectDescriptor(String name) {
        this.name = name;
        this.fields = Collections.emptyList();
        this.fieldsByName = new HashMap<String, FieldDescriptor>();
    }

    void initializeFields(List<FieldDescriptor> fields) {
        if (this.fields.equals(Collections.emptyList())) {
            this.fields = Collections.unmodifiableList(fields);

            for (FieldDescriptor field : fields) {
                fieldsByName.put(field.getName(), field);
            }
        } else {
            throw new IllegalStateException("Fields have already been initialized");
        }

        if (hasAttributesField()) {
            if (!Map.class.isAssignableFrom(getAttributesField().getFieldClass()))
                throw new ObjectDefinitionException(name, "'attributes' field has wrong Java type, must be Map<String, String>");
        }

        if (hasIdField()) {
            if (!String.class.isAssignableFrom(getIdField().getFieldClass()))
                throw new ObjectDefinitionException(name, "'id' field has wrong Java type, must be String");
        }
    }

    public boolean hasField(String name) {
        return fieldsByName.get(name) != null;
    }

    public boolean hasAttributesField() {
        return hasField(ATTRIBUTES_FIELD_NAME);
    }

    public boolean hasIdField() {
        return hasField(ID_FIELD_NAME);
    }

    public FieldDescriptor getField(String name) {
        FieldDescriptor descriptor = fieldsByName.get(name);
        if (descriptor == null) {
            throw new IllegalStateException(String.format("The object does not have a \'%s\' field", name));
        }
        return descriptor;
    }

    public FieldDescriptor getIdField() {
        return getField(ID_FIELD_NAME);
    }

    public FieldDescriptor getAttributesField() {
        return getField(ATTRIBUTES_FIELD_NAME);
    }

    public String getName() {
        return name;
    }

    public List<FieldDescriptor> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(name)
            .append(fields)
            .build();
    }
}
