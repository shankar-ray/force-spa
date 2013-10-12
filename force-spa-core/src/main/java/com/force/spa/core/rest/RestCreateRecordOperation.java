/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.CompletionHandler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.force.spa.CreateRecordOperation;
import com.force.spa.ObjectNotFoundException;
import com.force.spa.RecordResponseException;
import com.force.spa.core.utils.CountingJsonParser;
import com.google.common.base.Stopwatch;

final class RestCreateRecordOperation<T> extends AbstractRestRecordOperation<T, String> implements CreateRecordOperation<T> {

    private final T record;

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
    protected RuntimeException exceptionFor(int status, JsonParser parser) {
        if (status == 404) {
            throw new ObjectNotFoundException(buildErrorMessage(status, parser));
        } else {
            return super.exceptionFor(status, parser);
        }
    }

    @Override
    protected void start(RestConnector connector, final Stopwatch stopwatch) {

        final String json = encodeRecordForCreate(record);

        setTitle("Create " + getObjectDescriptor().getName());
        setDetail(json);

        URI uri = URI.create("/sobjects/" + getObjectDescriptor().getName());
        connector.post(uri, json, new CompletionHandler<CountingJsonParser, Integer>() {
            @Override
            public void completed(CountingJsonParser parser, Integer status) {
                checkStatus(status, parser);
                RestCreateRecordOperation.this.completed(parseResponseUsing(parser), buildStatistics(json, parser, stopwatch));
            }

            @Override
            public void failed(Throwable exception, Integer status) {
                RestCreateRecordOperation.this.failed(exception, buildStatistics(json, null, stopwatch));
            }
        });
    }

    private String encodeRecordForCreate(Object record) {
        try {
            return getMappingContext().getObjectWriterForCreate().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to encode record as JSON", e);
        }
    }

    private String parseResponseUsing(JsonParser parser) {
        JsonNode node = readTree(parser);
        if (!node.has("success") || !node.has("id")) {
            throw new RecordResponseException("JSON response is missing expected fields");
        }

        if (!node.get("success").asBoolean()) {
            throw new RecordResponseException(getErrorsText(node));
        }

        return node.get("id").asText();
    }

    private static JsonNode readTree(JsonParser parser) {
        try {
            return parser.readValueAsTree();
        } catch (IOException e) {
            throw new RecordResponseException("Failed to parse JSON response", e);
        }
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
        return "Salesforce REST error with no message";
    }
}
