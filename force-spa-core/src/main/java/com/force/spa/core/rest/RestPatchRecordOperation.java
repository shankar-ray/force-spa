/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.CompletionHandler;

import com.force.spa.PatchRecordOperation;
import com.force.spa.RecordResponseException;
import com.force.spa.core.utils.CountingJsonParser;
import com.google.common.base.Stopwatch;

class RestPatchRecordOperation<T> extends AbstractRestRecordOperation<T, Void> implements PatchRecordOperation<T> {

    private final String id;
    private final T record;

    @SuppressWarnings("unchecked")
    public RestPatchRecordOperation(RestRecordAccessor accessor, String id, T record) {
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

        final String json = encodeRecordForPatch(record);

        setTitle("Patch " + getObjectDescriptor().getName());
        setDetail(json);

        URI uri = URI.create("/sobjects/" + getObjectDescriptor().getName() + "/" + id);
        connector.patch(uri, json, new CompletionHandler<CountingJsonParser, Integer>() {
            @Override
            public void completed(CountingJsonParser parser, Integer status) {
                checkStatus(status, parser);
                RestPatchRecordOperation.this.completed(null, buildStatistics(json, null, stopwatch));
            }

            @Override
            public void failed(Throwable exception, Integer status) {
                RestPatchRecordOperation.this.failed(exception, buildStatistics(null, null, stopwatch));
            }
        });
    }

    private String encodeRecordForPatch(Object record) {
        try {
            return getMappingContext().getObjectWriterForPatch().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to encode record as JSON", e);
        }
    }
}
