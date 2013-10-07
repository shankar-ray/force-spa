/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

class SpaTypeIdResolver implements TypeIdResolver {
    private final JavaType baseType;
    private final MappingContext mappingContext;

    SpaTypeIdResolver(JavaType baseType, MappingContext mappingContext) {
        this.baseType = baseType;
        this.mappingContext = mappingContext;
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }

    @Override
    public void init(JavaType baseType) {
        // Not used
    }

    @Override
    public String idFromValue(Object value) {
        if (value != null) {
            ObjectDescriptor object = mappingContext.findObjectDescriptor(value.getClass());
            if (object != null) {
                return object.getName();
            }
        }
        return null;
    }

    @Override
    public JavaType typeFromId(String name) {
        ObjectDescriptor object = mappingContext.findObjectDescriptor(name);
        return (object != null) ? object.getJavaType() : null;
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public String idFromBaseType() {
        return idFromValueAndType(null, baseType.getRawClass());
    }
}
