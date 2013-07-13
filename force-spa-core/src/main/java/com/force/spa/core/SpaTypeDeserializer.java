/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;

public class SpaTypeDeserializer extends TypeDeserializerBase {
    private static final long serialVersionUID = 4715905537436577142L;

    public SpaTypeDeserializer(JavaType baseType, TypeIdResolver idResolver, Class<?> defaultImpl) {
        super(baseType, idResolver, "attributes", true, defaultImpl);
    }

    private SpaTypeDeserializer(SpaTypeDeserializer source, BeanProperty property) {
        super(source, property);
    }

    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.PROPERTY;
    }

    @Override
    public TypeDeserializer forProperty(BeanProperty property) {
        return (property == _property) ? this : new SpaTypeDeserializer(this, property);
    }

    @Override
    public Object deserializeTypedFromArray(JsonParser parser, DeserializationContext context) throws IOException {
        throw new UnsupportedOperationException("What are we doing here and how should we handle this?");
    }

    @Override
    public Object deserializeTypedFromScalar(JsonParser parser, DeserializationContext context) throws IOException {
        throw new UnsupportedOperationException("What are we doing here and how should we handle this?");
    }

    @Override
    public Object deserializeTypedFromAny(JsonParser parser, DeserializationContext context) throws IOException {
        throw new UnsupportedOperationException("What are we doing here and how should we handle this?");
    }

    @Override
    public Object deserializeTypedFromObject(JsonParser parser, DeserializationContext context) throws IOException {
        // Peek ahead for type information then put things back the way they were.
        TokenBuffer tokenBuffer = new TokenBuffer(null);
        String typeId = extractTypeIdFromObject(parser, tokenBuffer);
        parser = pushTokensBackOntoParser(parser, tokenBuffer);

        return _findDeserializer(context, typeId).deserialize(parser, context);
    }

    private static String extractTypeIdFromObject(JsonParser parser, TokenBuffer tokenBuffer) throws IOException {
        if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
            tokenBuffer.writeStartObject();
            parser.nextToken();
        }

        while (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
            String name = parser.getCurrentName();
            tokenBuffer.writeFieldName(name);
            parser.nextToken();
            if (name.equals("attributes")) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    tokenBuffer.writeStartObject();
                    parser.nextToken();
                    return extractTypeIdFromAttributes(parser, tokenBuffer);
                }
            }
            tokenBuffer.copyCurrentStructure(parser);
            parser.nextToken();
        }
        return null;
    }

    private static String extractTypeIdFromAttributes(JsonParser parser, TokenBuffer tokenBuffer) throws IOException {
        String typeId = null;
        while (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            tokenBuffer.writeFieldName(fieldName);
            parser.nextToken();
            if (fieldName.equals("type")) {
                typeId = parser.getText();
            }
            tokenBuffer.copyCurrentStructure(parser);
            parser.nextToken();
        }
        return typeId;
    }

    private static JsonParser pushTokensBackOntoParser(JsonParser parser, TokenBuffer tokenBuffer) throws IOException {
        tokenBuffer.copyCurrentStructure(parser);
        parser = JsonParserSequence.createFlattened(tokenBuffer.asParser(parser), parser);
        parser.nextToken();
        return parser;
    }
}
