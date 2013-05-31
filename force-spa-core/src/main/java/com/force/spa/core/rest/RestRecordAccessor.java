/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import com.force.spa.RestConnector;
import com.force.spa.CreateRecordOperation;
import com.force.spa.DeleteRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordOperation;
import com.force.spa.UpdateRecordOperation;
import com.force.spa.core.AbstractRecordAccessor;

import java.util.List;

/**
 * An implementation of {@link com.force.spa.RecordAccessor} that is based on the JSON representations of the Salesforce
 * REST API.
 */
public final class RestRecordAccessor extends AbstractRecordAccessor {

    private final RestConnector connector;

    public RestRecordAccessor(RestConnector connector) {
        this.connector = connector;
    }

    @Override
    public <T> void execute(List<RecordOperation<T>> operations) {
        RestConnector batchedConnector = new BatchRestConnector(connector);
        for (RecordOperation<?> operation : operations) {
            if (operation instanceof RestRecordOperation) {
                ((RestRecordOperation) operation).start(batchedConnector, getMappingContext());
            } else {
                throw new IllegalArgumentException("operation isn't supported because it doesn't implement RestRecordOperation");
            }
        }
        batchedConnector.flush();
    }

    @Override
    protected void execute(RecordOperation<?> operation) {
        if (operation instanceof RestRecordOperation) {
            ((RestRecordOperation) operation).start(connector, getMappingContext());
            connector.flush();
        } else {
            throw new IllegalArgumentException("operation isn't supported because it doesn't implement RestRecordOperation");
        }
    }

    @Override
    public CreateRecordOperation newCreateRecordOperation(Object record) {
        return new RestCreateRecordOperation(record);
    }

    @Override
    public DeleteRecordOperation newDeleteRecordOperation(String id, Class<?> recordClass) {
        return new RestDeleteRecordOperation(id, recordClass);
    }

    @Override
    public <T> GetRecordOperation<T> newGetRecordOperation(String id, Class<T> recordClass) {
        return new RestGetRecordOperation<T>(id, recordClass);
    }

    @Override
    public PatchRecordOperation newPatchRecordOperation(String id, Object record) {
        return new RestPatchRecordOperation(id, record);
    }

    @Override
    public <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soql, Class<T> recordClass) {
        return new RestQueryRecordsOperation<T>(soql, recordClass);
    }

    @Override
    public <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soql, Class<?> recordClass, Class<T> resultClass) {
        return new RestQueryRecordsOperation<T>(soql, recordClass, resultClass);
    }

    @Override
    public UpdateRecordOperation newUpdateRecordOperation(String id, Object record) {
        return new RestUpdateRecordOperation(id, record);
    }
}
