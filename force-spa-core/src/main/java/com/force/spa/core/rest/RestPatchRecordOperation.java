/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;

import com.force.spa.PatchRecordOperation;
import com.force.spa.RecordResponseException;

class RestPatchRecordOperation<T> extends AbstractRestOperation<T, Void> implements PatchRecordOperation<T> {

    private final String id;
    private final T record;

    private String jsonBody;

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
    protected void start(RestConnector connector) {

        jsonBody = serializeRecord(record);

        URI uri = URI.create("/sobjects/" + getObjectDescriptor().getName() + "/" + id);
        connector.patch(uri, jsonBody, new ResponseHandler());
    }

    private String serializeRecord(Object record) {
        try {
            return getMappingContext().getObjectWriterForPatch().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to serialize record", e);
        }
    }

    @Override
    public String toString() {
        String string = "Patch " + getObjectDescriptor().getName() + " with id " + id;
        if (getLogger().isDebugEnabled()) {
            string += ": " + jsonBody;
        }
        return string;
    }
}
