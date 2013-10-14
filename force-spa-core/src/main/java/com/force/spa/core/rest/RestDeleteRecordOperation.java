/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.net.URI;

import com.force.spa.DeleteRecordOperation;

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
    protected void start(RestConnector connector) {

        URI uri = URI.create("/sobjects/" + getObjectDescriptor().getName() + "/" + id);
        connector.delete(uri, new ResponseHandler());
    }

    @Override
    public String toString() {
        return "Delete " + getObjectDescriptor().getName() + " with id " + id;
    }
}
