/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.apache.commons.lang3.Validate;

import java.io.IOException;

/**
 * A special deserializer that knows how to deal with the Salesforce REST representation for SOQL subqueries.
 * <p/>
 * What we really want from a subquery result is the array of records that the subquery returns. Standard Jackson
 * deserializers know how to deal with arrays of records but the the Salesforce REST representation wraps the array of
 * records with a little extra metadata that Jackson doesn't know how to deal with. You can find detailed information on
 * the returned representation in the Salesforce REST API Developer's Guide. Basically it is an object with several
 * properties about the subquery results. One of the object properties, the "records" property, holds the array of
 * records we are interested in.
 * <p/>
 * This special deserializer helps to wade through the extra metadata and arrive at the raw array of records which the
 * standard Jackson deserializers can then handle in the normal way.
 */
class SubqueryDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private final JsonDeserializer<?> delegate;

    /**
     * Constructs a new instance which wraps a standard Jackson deserializer that knows how to deal with the array of
     * raw records once we have waded through the extra wrapping.
     *
     * @param delegate the deserializer that knows how to deal with the unwrapped array of records
     */
    SubqueryDeserializer(JsonDeserializer<?> delegate) {
        Validate.isTrue(delegate instanceof ContextualDeserializer);

        this.delegate = delegate;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) throws JsonMappingException {
        return new SubqueryDeserializer(((ContextualDeserializer) delegate).createContextual(context, property));
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
            jp.nextToken();

            Object result = null;
            for (JsonToken token = jp.getCurrentToken(); token == JsonToken.FIELD_NAME; token = jp.nextToken()) {
                String fieldName = jp.getCurrentName();
                jp.nextValue();

                if (fieldName.equals("records")) {
                    result = delegate.deserialize(jp, context); // Delegate the raw records to the standard deserializer
                }
            }
            return result;
        } else {
            return delegate.deserialize(jp, context);
        }
    }
}


