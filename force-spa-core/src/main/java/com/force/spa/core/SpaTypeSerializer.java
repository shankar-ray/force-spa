/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeSerializerBase;

class SpaTypeSerializer extends TypeSerializerBase {

    private final MappingContext mappingContext;

    SpaTypeSerializer(MappingContext mappingContext, TypeIdResolver idResolver) {
        super(idResolver, null);
        this.mappingContext = mappingContext;
    }

    private SpaTypeSerializer(SpaTypeSerializer source, BeanProperty property) {
        super(source._idResolver, property);
        this.mappingContext = source.mappingContext;
    }

    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.PROPERTY;
    }

    @Override
    public TypeSerializer forProperty(BeanProperty property) {
        return (property == _property) ? this : new SpaTypeSerializer(this, property);
    }

    @Override
    public void writeTypePrefixForScalar(Object value, JsonGenerator generator) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void writeTypePrefixForObject(Object instance, JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(instance.getClass());
        if (hasAttributes(descriptor, instance)) {
            AttributesSerializer.setTypeForInclusionInAttributes(descriptor.getName());
        } else {
            writeAttributesWithTypeNow(generator, descriptor);
        }
    }

    @Override
    public void writeTypePrefixForArray(Object value, JsonGenerator generator) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void writeTypeSuffixForScalar(Object value, JsonGenerator generator) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void writeTypeSuffixForObject(Object value, JsonGenerator generator) throws IOException {
        generator.writeEndObject();
    }

    @Override
    public void writeTypeSuffixForArray(Object value, JsonGenerator generator) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void writeCustomTypePrefixForScalar(Object value, JsonGenerator generator, String typeId) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void writeCustomTypePrefixForObject(Object value, JsonGenerator generator, String typeId) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void writeCustomTypePrefixForArray(Object value, JsonGenerator generator, String typeId) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void writeCustomTypeSuffixForScalar(Object value, JsonGenerator generator, String typeId) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void writeCustomTypeSuffixForObject(Object value, JsonGenerator generator, String typeId) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void writeCustomTypeSuffixForArray(Object value, JsonGenerator generator, String typeId) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static boolean hasAttributes(ObjectDescriptor object, Object record) {
        if (object.hasAttributesField()) {
            Map<String, String> attributes = object.getAttributesField().getValue(record);
            if (attributes != null && attributes.size() > 0)
                return true;
        }
        return false;
    }

    private static void writeAttributesWithTypeNow(JsonGenerator generator, ObjectDescriptor descriptor) throws IOException {
        generator.writeFieldName(ObjectDescriptor.ATTRIBUTES_FIELD_NAME);
        generator.writeStartObject();
        generator.writeStringField("type", descriptor.getName());
        generator.writeEndObject();
    }
}
