/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeSerializerBase;

import java.io.IOException;

public class SpaTypeSerializer extends TypeSerializerBase {

    public SpaTypeSerializer(TypeIdResolver idResolver) {
        super(idResolver, null);
    }

    private SpaTypeSerializer(SpaTypeSerializer source, BeanProperty property) {
        super(source._idResolver, property);
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
    public void writeTypePrefixForObject(Object value, JsonGenerator generator) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
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
        throw new UnsupportedOperationException("Not implemented yet");
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
}
