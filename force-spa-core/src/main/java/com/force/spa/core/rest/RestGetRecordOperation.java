/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.core.JsonParser;
import com.force.spa.GetRecordOperation;
import com.force.spa.RecordNotFoundException;
import com.force.spa.core.SoqlBuilder;
import com.force.spa.core.utils.CountingJsonParser;
import com.google.common.net.UrlEscapers;

class RestGetRecordOperation<T> extends AbstractRestRecordOperation<T, T> implements GetRecordOperation<T> {

    private final String id;
    private final Class<T> recordClass;

    private String soql;

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
    protected void start(RestConnector connector) {

        soql = new SoqlBuilder(getRecordAccessor())
            .object(getObjectDescriptor())
            .template("SELECT * FROM " + getObjectDescriptor().getName() + " WHERE Id='" + id + "'")
            .limit(1)
            .build();

        URI uri = URI.create("/query?q=" + UrlEscapers.urlFormParameterEscaper().escape(soql));
        connector.get(uri, new ResponseHandler() {
            @Override
            public T deserialize(CountingJsonParser parser) throws IOException {
                return deserializeRecord(parser);
            }
        });
    }

    private T deserializeRecord(JsonParser parser) throws IOException {
        QueryResult queryResult = parser.readValueAs(QueryResult.class);
        if (queryResult.getRecords() == null || queryResult.getRecords().size() < 1) {
            throw new RecordNotFoundException(id);
        }
        return recordClass.cast(queryResult.getRecords().get(0));
    }

    @Override
    public String toString() {
        String string = "Get " + getObjectDescriptor().getName() + " with id " + id;
        if (getLogger().isDebugEnabled()) {
            string += ": " + soql;
        }
        return string;
    }
}
