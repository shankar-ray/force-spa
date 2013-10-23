/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.force.spa.CreateRecordOperation;
import com.force.spa.ObjectNotFoundException;
import com.force.spa.RecordResponseException;
import com.force.spa.core.utils.CountingJsonParser;

/**
 * @param <T> the type of record the operation is working with
 */
final class RestCreateRecordOperation<T> extends AbstractRestRecordOperation<T, String> implements CreateRecordOperation<T> {

    private final T record;

    private String jsonBody;

    @SuppressWarnings("unchecked")
    public RestCreateRecordOperation(RestRecordAccessor accessor, T record) {
        super(accessor, (Class<T>) record.getClass());

        this.record = record;
    }

    @Override
    public T getRecord() {
        return record;
    }

    @Override
    protected void start(RestConnector connector) {

        jsonBody = serializeRecord(record);

        URI uri = URI.create("/sobjects/" + getObjectDescriptor().getName());
        connector.post(uri, jsonBody, new ResponseHandler() {
            @Override
            public String deserialize(CountingJsonParser parser) throws IOException {
                return deserializeId(parser);
            }

            @Override
            public void handleStatus(int status, JsonParser parser) {
                if (status == 404) {
                    throw new ObjectNotFoundException(getExceptionMessage(status, parser));
                } else {
                    super.handleStatus(status, parser);
                }
            }
        });
    }

    private String serializeRecord(Object record) {
        try {
            return getMappingContext().getObjectWriterForCreate().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to serialize record", e);
        }
    }

    private String deserializeId(JsonParser parser) throws IOException {
        JsonNode node = parser.readValueAsTree();
        if (node.has("success") && !node.get("success").asBoolean()) {
            throw new RecordResponseException(getErrorMessage(node));
        }

        if (node.has("id")) {
            return node.get("id").asText();
        } else {
            throw new JsonParseException("Missing 'id' field", parser.getCurrentLocation());
        }
    }

    // The error message can sometimes come in this different place for "Create".
    private static String getErrorMessage(JsonNode node) {
        if (node.has("errors")) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode error : node.get("errors")) {
                if (sb.length() > 0)
                    sb.append("; ");
                sb.append(error.asText());
            }
            if (sb.length() > 0)
                return sb.toString();
        }
        return "Salesforce REST Create error with no message";
    }

    @Override
    public String toString() {
        String string = "Create " + getObjectDescriptor().getName();
        if (getLogger().isTraceEnabled()) {
            string += ": " + jsonBody;
        }
        return string;
    }
}
