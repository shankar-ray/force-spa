/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.util.List;

import com.force.spa.ApiVersion;
import com.force.spa.CreateRecordOperation;
import com.force.spa.DeleteRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordOperation;
import com.force.spa.RestConnector;
import com.force.spa.UpdateRecordOperation;
import com.force.spa.core.AbstractRecordAccessor;

/**
 * An implementation of {@link com.force.spa.RecordAccessor} that is based on the JSON representations of the Salesforce
 * REST API.
 */
public final class RestRecordAccessor extends AbstractRecordAccessor {

    private static final ApiVersion MINIMUM_VERSION_FOR_BATCHING = new ApiVersion(29, 0);

    private final RestConnector connector;

    public RestRecordAccessor(RecordAccessorConfig config, RestConnector connector) {
        super(config, new RestMetadataAccessor(config, connector));
        this.connector = connector;
    }

    @Override
    protected void execute(RecordOperation<?> operation) {
        if (operation instanceof RestRecordOperation) {
            ((RestRecordOperation) operation).start(connector);
            connector.flush();
        } else {
            throw new IllegalArgumentException("operation isn't supported because it doesn't implement RestRecordOperation");
        }
    }

    @Override
    public void execute(List<RecordOperation<?>> operations) {
        RestConnector batchConnector = isBatchingSupported() ? new BatchRestConnector(connector) : connector;
        for (RecordOperation<?> operation : operations) {
            if (operation instanceof RestRecordOperation) {
                ((RestRecordOperation) operation).start(batchConnector);
            } else {
                throw new IllegalArgumentException("operation isn't supported because it doesn't implement RestRecordOperation");
            }
        }
        batchConnector.flush(); // Causes all the buffered operations to be sent (executed).
    }

    @Override
    public <T> CreateRecordOperation<T> newCreateRecordOperation(T record) {
        return new RestCreateRecordOperation<T>(this, record);
    }

    @Override
    public <T> DeleteRecordOperation<T> newDeleteRecordOperation(String id, Class<T> recordClass) {
        return new RestDeleteRecordOperation<T>(this, id, recordClass);
    }

    @Override
    public <T> GetRecordOperation<T> newGetRecordOperation(String id, Class<T> recordClass) {
        return new RestGetRecordOperation<T>(this, id, recordClass);
    }

    @Override
    public <T> PatchRecordOperation<T> newPatchRecordOperation(String id, T record) {
        return new RestPatchRecordOperation<T>(this, id, record);
    }

    @Override
    public <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soql, Class<T> recordClass) {
        return new RestQueryRecordsOperation<T>(this, soql, recordClass);
    }

    @Override
    public <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soql, Class<?> recordClass, Class<T> resultClass) {
        return new RestQueryRecordsOperation<T>(this, soql, recordClass, resultClass);
    }

    @Override
    public <T> UpdateRecordOperation<T> newUpdateRecordOperation(String id, T record) {
        return new RestUpdateRecordOperation<T>(this, id, record);
    }

    /**
     * For unit test purposes only.
     */
    public RestConnector getConnector() {
        return connector;
    }

    private boolean isBatchingSupported() {
        return MINIMUM_VERSION_FOR_BATCHING.compareTo(connector.getApiVersion()) <= 0;
    }
}
