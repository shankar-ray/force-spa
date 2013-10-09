/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.util.List;

import org.apache.commons.lang3.Validate;

import com.force.spa.ApiVersion;
import com.force.spa.CreateRecordOperation;
import com.force.spa.DeleteRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordOperation;
import com.force.spa.UpdateRecordOperation;
import com.force.spa.core.AbstractRecordAccessor;
import com.force.spa.core.MappingContext;
import com.google.common.base.Stopwatch;

/**
 * An implementation of {@link com.force.spa.RecordAccessor} that is based on the JSON representations of the Salesforce
 * REST API.
 */
public final class RestRecordAccessor extends AbstractRecordAccessor {

    private static final ApiVersion MINIMUM_VERSION_FOR_BATCHING = new ApiVersion(29, 0);

    private final RestConnector connector;

    public RestRecordAccessor(RecordAccessorConfig config, MappingContext mappingContext, RestConnector connector) {
        super(config, mappingContext, new RestMetadataAccessor(config, mappingContext, connector));
        this.connector = connector;
    }

    @Override
    public void execute(List<RecordOperation<?>> operations) {
        RestConnector connector = shouldBatch(operations) ? new BatchRestConnector(this.connector) : this.connector;

        for (RecordOperation<?> operation : operations) {
            AbstractRestRecordOperation.class.cast(operation).start(connector, Stopwatch.createStarted());
        }
        connector.join(); // Wait for all the operations to complete
    }

    @Override
    public <T> CreateRecordOperation<T> newCreateRecordOperation(T record) {

        Validate.notNull(record, "record must not be null");

        return new RestCreateRecordOperation<T>(this, record);
    }

    @Override
    public <T> DeleteRecordOperation<T> newDeleteRecordOperation(String id, Class<T> recordClass) {

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");

        return new RestDeleteRecordOperation<T>(this, id, recordClass);
    }

    @Override
    public <T> GetRecordOperation<T> newGetRecordOperation(String id, Class<T> recordClass) {

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");

        return new RestGetRecordOperation<T>(this, id, recordClass);
    }

    @Override
    public <T> PatchRecordOperation<T> newPatchRecordOperation(String id, T record) {

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(record, "record must not be null");

        return new RestPatchRecordOperation<T>(this, id, record);
    }

    @Override
    public <T> QueryRecordsOperation<T, T> newQueryRecordsOperation(String soql, Class<T> recordClass) {

        Validate.notEmpty(soql, "soql must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");

        return new RestQueryRecordsOperation<T, T>(this, soql, recordClass, recordClass);
    }

    @Override
    public <T, R> QueryRecordsOperation<T, R> newQueryRecordsOperation(String soqlTemplate, Class<T> recordClass, Class<R> resultClass) {

        Validate.notEmpty(soqlTemplate, "soqlTemplate must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");
        Validate.notNull(resultClass, "resultClass must not be null");

        return new RestQueryRecordsOperation<T, R>(this, soqlTemplate, recordClass, resultClass);
    }

    @Override
    public <T> UpdateRecordOperation<T> newUpdateRecordOperation(String id, T record) {

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(record, "record must not be null");

        return new RestUpdateRecordOperation<T>(this, id, record);
    }

    public RestConnector getConnector() {  // For unit test purposes only.
        return connector;
    }

    private boolean shouldBatch(List<RecordOperation<?>> operations) {
        return operations.size() > 1 && isBatchingSupported();
    }

    private boolean isBatchingSupported() {
        return MINIMUM_VERSION_FOR_BATCHING.compareTo(connector.getApiVersion()) <= 0;
        //TODO Need to check perm too, log message if perm not turned on...   or something like that, probably config option too
    }
}
