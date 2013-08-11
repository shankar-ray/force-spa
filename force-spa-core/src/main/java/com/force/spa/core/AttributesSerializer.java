/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A special version of MapSerializer that is used to serialize the object "attributes". This is needed so we can merge
 * our own information with caller specified information sometimes. For example, sometimes we need to add the "type"
 * entry to attributes already specified by the caller.
 */
final class AttributesSerializer extends JsonSerializer<Map<String, String>> {

    private static final ThreadLocal<String> typeForInclusionInAttributes = new ThreadLocal<String>();

    /**
     * A way for the SpaTypeSerializer to indicate that type information needs to be added to the attributes.
     * <p/>
     * Sorry for the thread local technique but it was the most pragmatic. The two classes don't share any context so
     * direct passage was not possible. The thread local technique works because the "attributes" are always the first
     * field to be serialized in an object (before any other nested objects are serialized. structure to store the
     * data.
     */
    static void setTypeForInclusionInAttributes(String name) {
        typeForInclusionInAttributes.set(name);
    }

    static String consumeTypeForInclusionInAttributes() {
        String name = typeForInclusionInAttributes.get();
        typeForInclusionInAttributes.set(null);
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(Map<String, String> value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        String typeName = consumeTypeForInclusionInAttributes();
        if (typeName != null && value.get("type") == null) {
            Map<String, String> augmentedValue = new HashMap<String, String>(value);
            augmentedValue.put("type", typeName);
            serializeMap(augmentedValue, generator, provider);
        } else {
            serializeMap(value, generator, provider);
        }
    }

    private void serializeMap(Map<String, String> map, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        if (provider.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)) {
            map = new TreeMap<String, String>(map);
        }

        boolean writeNulls = provider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() != null || writeNulls) {
                generator.writeStringField(entry.getKey(), entry.getValue());
            }
        }
        generator.writeEndObject();
    }
}
