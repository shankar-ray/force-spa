/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeNameIdResolver;

import java.util.Collection;

public class SpaTypeResolverBuilder implements TypeResolverBuilder<SpaTypeResolverBuilder> {
    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        TypeIdResolver idRes = TypeNameIdResolver.construct(config, baseType, subtypes, false, true);
        return new AsPropertyTypeDeserializer(baseType, idRes,
            "attributes", false, null);

//        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<?> getDefaultImpl() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        TypeIdResolver idRes = TypeNameIdResolver.construct(config, baseType, subtypes, false, true);
        return new AsPropertyTypeSerializer(idRes, null, "attributes");
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SpaTypeResolverBuilder init(JsonTypeInfo.Id idType, TypeIdResolver res) {
        return this;
    }

    @Override
    public SpaTypeResolverBuilder inclusion(JsonTypeInfo.As includeAs) {
        return this;
    }

    @Override
    public SpaTypeResolverBuilder typeProperty(String propName) {
        return this;
    }

    @Override
    public SpaTypeResolverBuilder defaultImpl(Class<?> defaultImpl) {
        return this;
    }

    @Override
    public SpaTypeResolverBuilder typeIdVisibility(boolean isVisible) {
        return this;
    }
}
