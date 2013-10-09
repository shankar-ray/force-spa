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
import com.force.spa.GetRecordOperation;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordResponseException;
import com.force.spa.core.CountingJsonParser;
import com.force.spa.core.SoqlBuilder;
import com.google.common.base.Stopwatch;
import com.google.common.net.UrlEscapers;

class RestGetRecordOperation<T> extends AbstractRestRecordOperation<T, T> implements GetRecordOperation<T> {

    private final String id;
    private final Class<T> recordClass;

    public RestGetRecordOperation(RestRecordAccessor accessor, String id, Class<T> recordClass) {
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

        String soql = new SoqlBuilder(getRecordAccessor())
            .object(getObjectDescriptor())
            .template("SELECT * FROM " + getObjectDescriptor().getName() + " WHERE Id='" + id + "'")
            .limit(1)
            .build();

        setTitle("Get " + getObjectDescriptor().getName());
        setDetail(soql);

        URI uri = URI.create("/query?q=" + UrlEscapers.urlFormParameterEscaper().escape(soql));
        connector.get(uri, new CompletionHandler<CountingJsonParser, Integer>() {
            @Override
            public void completed(CountingJsonParser parser, Integer status) {
                checkStatus(status, parser);
                RestGetRecordOperation.this.completed(parseResponseUsing(parser), buildStatistics(null, parser, stopwatch));
            }

            @Override
            public void failed(Throwable exception, Integer status) {
                RestGetRecordOperation.this.failed(exception, buildStatistics(null, null, stopwatch));
            }
        });
    }

    private T parseResponseUsing(JsonParser parser) {
        try {
            QueryResult queryResult = parser.readValueAs(QueryResult.class);
            if (queryResult.getRecords() == null || queryResult.getRecords().size() < 1) {
                throw new RecordNotFoundException();
            }
            return recordClass.cast(queryResult.getRecords().get(0));
        } catch (IOException e) {
            throw new RecordResponseException("Failed to decode JSON record", e);
        }
    }
}
