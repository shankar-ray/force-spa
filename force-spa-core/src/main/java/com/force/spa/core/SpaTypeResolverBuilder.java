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
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeNameIdResolver;

class SpaTypeResolverBuilder implements TypeResolverBuilder<SpaTypeResolverBuilder> {

    private Class<?> defaultImpl = null;
    private TypeIdResolver customIdResolver = null;
    private final ObjectMappingContext mappingContext;

    SpaTypeResolverBuilder(ObjectMappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        TypeIdResolver idResolver = getIdResolver(config, baseType, subtypes, false);
        return new SpaTypeDeserializer(baseType, idResolver, defaultImpl != null ? defaultImpl : baseType.getRawClass());
    }

    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        TypeIdResolver idResolver = getIdResolver(config, baseType, subtypes, true);
        return new SpaTypeSerializer(mappingContext, idResolver);
    }

    @Override
    public SpaTypeResolverBuilder init(JsonTypeInfo.Id idType, TypeIdResolver idResolver) {
        this.customIdResolver = idResolver;
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
        throw new UnsupportedOperationException("Type visibility is not configurable");
    }

    private TypeIdResolver getIdResolver(MapperConfig<?> config, JavaType baseType, Collection<NamedType> subtypes, boolean forSerialization) {
        if (customIdResolver != null) {
            return customIdResolver;
        } else {
            return TypeNameIdResolver.construct(config, baseType, subtypes, forSerialization, !forSerialization);
        }
    }
}
