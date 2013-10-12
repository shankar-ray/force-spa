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

class RestUpdateRecordOperation<T> extends AbstractRestRecordOperation<T, Void> implements UpdateRecordOperation<T> {

    private final String id;
    private final T record;

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
    protected void start(RestConnector connector, final Stopwatch stopwatch) {

        final String json = encodeRecordForUpdate(record);

        setTitle("Update " + getObjectDescriptor().getName());
        setDetail(json);

        URI uri = URI.create("/sobjects/" + getObjectDescriptor().getName() + "/" + id);
        connector.patch(uri, json, new CompletionHandler<CountingJsonParser, Integer>() {
            @Override
            public void completed(CountingJsonParser parser, Integer status) {
                checkStatus(status, parser);
                RestUpdateRecordOperation.this.completed(null, buildStatistics(json, null, stopwatch));
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
