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
import com.force.spa.DescribeObjectOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.Operation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordAccessorConfig;
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
        super(config, mappingContext);
        this.connector = connector;
    }

    @Override
    public void execute(List<Operation<?>> operations) {

        RestConnector connector = chooseBatchedOrUnbatchedConnector(operations);
        for (Operation<?> operation : operations) {
            AbstractRestOperation.class.cast(operation).start(connector, Stopwatch.createStarted());
        }
        connector.join(); // Wait for all the operations to complete
    }

    @Override
    public <T> CreateRecordOperation<T> newCreateRecordOperation(T record) {

        Validate.notNull(record, "record must not be null");

        return new RestCreateRecordOperation<>(this, record);
    }

    @Override
    public <T> DeleteRecordOperation<T> newDeleteRecordOperation(String id, Class<T> recordClass) {

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");

        return new RestDeleteRecordOperation<>(this, id, recordClass);
    }

    @Override
    public DescribeObjectOperation newDescribeObjectOperation(String name) {

        Validate.notEmpty(name, "name must not be empty");

        return new RestDescribeObjectOperation(this, name);
    }

    @Override
    public <T> GetRecordOperation<T> newGetRecordOperation(String id, Class<T> recordClass) {

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");

        return new RestGetRecordOperation<>(this, id, recordClass);
    }

    @Override
    public <T> PatchRecordOperation<T> newPatchRecordOperation(String id, T record) {

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(record, "record must not be null");

        return new RestPatchRecordOperation<>(this, id, record);
    }

    @Override
    public <T> QueryRecordsOperation<T, T> newQueryRecordsOperation(String soql, Class<T> recordClass) {

        Validate.notEmpty(soql, "soql must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");

        return new RestQueryRecordsOperation<>(this, soql, recordClass, recordClass);
    }

    @Override
    public <T, R> QueryRecordsOperation<T, R> newQueryRecordsOperation(String soqlTemplate, Class<T> recordClass, Class<R> resultClass) {

        Validate.notEmpty(soqlTemplate, "soqlTemplate must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");
        Validate.notNull(resultClass, "resultClass must not be null");

        return new RestQueryRecordsOperation<>(this, soqlTemplate, recordClass, resultClass);
    }

    @Override
    public <T> UpdateRecordOperation<T> newUpdateRecordOperation(String id, T record) {

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(record, "record must not be null");

        return new RestUpdateRecordOperation<>(this, id, record);
    }

    public RestConnector getConnector() {  // For unit test purposes only.
        return connector;
    }

    private RestConnector chooseBatchedOrUnbatchedConnector(List<Operation<?>> operations) {
        if (shouldBatch(operations)) {
            for (Operation<?> operation : operations) {
                AbstractRestOperation.class.cast(operation).setBatched(true);
            }
            return new BatchRestConnector(connector);
        } else {
            return connector;
        }

    }

    private boolean shouldBatch(List<Operation<?>> operations) {
        return operations.size() > 1 && isBatchingSupported();
    }

    private boolean isBatchingSupported() {
        //TODO Need to check perm too, log message if perm not turned on and maybe a config option too
        return MINIMUM_VERSION_FOR_BATCHING.compareTo(connector.getApiVersion()) <= 0;
    }
}
