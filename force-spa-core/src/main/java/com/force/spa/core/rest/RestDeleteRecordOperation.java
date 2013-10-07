/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.net.URI;
import java.nio.channels.CompletionHandler;

import com.force.spa.DeleteRecordOperation;
import com.force.spa.core.CountingJsonParser;
import com.google.common.base.Stopwatch;

class RestDeleteRecordOperation<T> extends AbstractRestRecordOperation<T, Void> implements DeleteRecordOperation<T> {

    private final String id;
    private final Class<T> recordClass;

    public RestDeleteRecordOperation(RestRecordAccessor accessor, String id, Class<T> recordClass) {
        super(accessor, recordClass);

        this.id = id;
        this.recordClass = recordClass;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Class<T> getRecordClass() {
        return recordClass;
    }

    @Override
    protected void start(RestConnector connector, final Stopwatch stopwatch) {

        setTitle("Delete " + getObjectDescriptor().getName());
        setDetail(id);

        URI uri = URI.create("/sobjects/" + getObjectDescriptor().getName() + "/" + id);
        connector.delete(uri, new CompletionHandler<CountingJsonParser, Integer>() {
            @Override
            public void completed(CountingJsonParser parser, Integer status) {
                checkStatus(status, parser);
                RestDeleteRecordOperation.this.completed(null, buildStatistics(null, null, stopwatch));
            }

            @Override
            public void failed(Throwable exception, Integer status) {
                RestDeleteRecordOperation.this.failed(exception, buildStatistics(null, null, stopwatch));
            }
        });
    }
}
