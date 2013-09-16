/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.force.spa.CreateRecordOperation;
import com.force.spa.RecordResponseException;
import com.force.spa.RestConnector;
import com.force.spa.core.ObjectDescriptor;

final class RestCreateRecordOperation<T> extends AbstractRestRecordOperation<String> implements CreateRecordOperation<T> {
    private static final Logger log = LoggerFactory.getLogger(RestCreateRecordOperation.class);

    private final T record;

    public RestCreateRecordOperation(RestRecordAccessor accessor, T record) {
        super(accessor);

        Validate.notNull(record, "record must not be null");

        this.record = record;
    }

    @Override
    public T getRecord() {
        return record;
    }

    @Override
    public void start(RestConnector connector) {
        final ObjectDescriptor descriptor = getObjectMappingContext().getRequiredObjectDescriptor(record.getClass());

        URI uri = URI.create("/sobjects/" + descriptor.getName());
        String json = encodeRecordForCreate(record);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Create %s: %s", descriptor.getName(), json));
        }

        connector.post(uri, json, new RestConnector.Callback<JsonNode>() {
            @Override
            public void onSuccess(JsonNode result) {
                if (!result.has("success") || !result.has("id")) {
                    throw new RecordResponseException("JSON response is missing expected fields");
                }
                if (!result.get("success").asBoolean()) {
                    throw new RecordResponseException(getErrorsText(result));
                }
                String id = result.get("id").asText();

                if (log.isDebugEnabled()) {
                    log.debug(String.format("...Created %s %s", descriptor.getName(), id));
                }
                set(id);
            }

            @Override
            public void onFailure(RuntimeException exception) {
                setException(exception);
            }
        });
    }

    private static String getErrorsText(JsonNode node) {
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
        return "Salesforce persistence error with no message";
    }

    private String encodeRecordForCreate(Object record) {
        try {
            return getObjectMappingContext().getObjectWriterForCreate().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to encode record as JSON", e);
        }
    }
}
