/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

class SpaTypeResolverBuilder implements TypeResolverBuilder<SpaTypeResolverBuilder> {

    private Class<?> defaultImpl = null;
    private final MappingContext mappingContext;

    SpaTypeResolverBuilder(MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        TypeIdResolver typeIdResolver = new SpaTypeIdResolver(baseType, mappingContext);
        return new SpaTypeDeserializer(baseType, typeIdResolver, defaultImpl != null ? defaultImpl : baseType.getRawClass());
    }

    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        TypeIdResolver typeIdResolver = new SpaTypeIdResolver(baseType, mappingContext);
        return new SpaTypeSerializer(mappingContext, typeIdResolver);
    }

    @Override
    public SpaTypeResolverBuilder init(JsonTypeInfo.Id idType, TypeIdResolver idResolver) {
        if (idResolver != null) {
            throw new UnsupportedOperationException("Type id resolver can not be externally specified");
        }
        return this;
    }

    @Override
    public SpaTypeResolverBuilder defaultImpl(Class<?> defaultImpl) {
        this.defaultImpl = defaultImpl;
        return this;
    }

    @Override
    public Class<?> getDefaultImpl() {
        return defaultImpl;
    }

    @Override
    public SpaTypeResolverBuilder inclusion(JsonTypeInfo.As includeAs) {
        throw new UnsupportedOperationException("Type inclusion mechanism is not configurable");
    }

    @Override
    public SpaTypeResolverBuilder typeProperty(String typePropertyName) {
        throw new UnsupportedOperationException("Type property name is not configurable");
    }

    @Override
    public SpaTypeResolverBuilder typeIdVisibility(boolean isVisible) {
        throw new UnsupportedOperationException("Type id visibility is not configurable");
    }
}
