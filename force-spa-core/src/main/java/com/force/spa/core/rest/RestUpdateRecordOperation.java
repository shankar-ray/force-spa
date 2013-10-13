/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.CompletionHandler;

import com.force.spa.RecordResponseException;
import com.force.spa.UpdateRecordOperation;
import com.force.spa.core.utils.CountingJsonParser;
import com.google.common.base.Stopwatch;

class RestUpdateRecordOperation<T> extends AbstractRestOperation<T, Void> implements UpdateRecordOperation<T> {

    private final String id;
    private final T record;

    private String jsonBody;

    @SuppressWarnings("unchecked")
    public RestUpdateRecordOperation(RestRecordAccessor accessor, String id, T record) {
        super(accessor, (Class<T>) record.getClass());

        this.id = id;
        this.record = record;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public T getRecord() {
        return record;
    }

    @Override
    public String toString() {
        String string = "Update " + getObjectDescriptor().getName() + " with id " + id;
        if (getLogger().isDebugEnabled()) {
            string += ": " + jsonBody;
        }
        return string;
    }

    @Override
    protected void start(RestConnector connector, final Stopwatch stopwatch) {

        jsonBody = encodeRecordForUpdate(record);

        URI uri = URI.create("/sobjects/" + getObjectDescriptor().getName() + "/" + id);
        connector.patch(uri, jsonBody, new CompletionHandler<CountingJsonParser, Integer>() {
            @Override
            public void completed(CountingJsonParser parser, Integer status) {
                checkStatus(status, parser);
                RestUpdateRecordOperation.this.completed(null, buildStatistics(jsonBody, null, stopwatch));
            }

            @Override
            public void failed(Throwable exception, Integer status) {
                RestUpdateRecordOperation.this.failed(exception, buildStatistics(null, null, stopwatch));
            }
        });
    }

    private String encodeRecordForUpdate(Object record) {
        try {
            return getMappingContext().getObjectWriterForUpdate().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to encode record as JSON", e);
        }
    }
}
