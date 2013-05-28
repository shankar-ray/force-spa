/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.force.spa.RecordAccessor;
import com.force.spa.RecordQuery;
import com.force.spa.RecordRequestException;
import com.force.spa.RestConnector;
import com.force.spa.batch.BatchRecordAccessor;
import com.force.spa.batch.BatchedRestConnector;
import com.force.spa.batch.CreateRecordOperation;
import com.force.spa.batch.DeleteRecordOperation;
import com.force.spa.batch.GetRecordOperation;
import com.force.spa.batch.PatchRecordOperation;
import com.force.spa.batch.QueryRecordsOperation;
import com.force.spa.batch.RecordOperation;
import com.force.spa.batch.UpdateRecordOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.util.List;

/**
 * An implementation of {@link RecordAccessor} that is based on the JSON representations of the Salesforce REST API.
 */
public final class RestRecordAccessor implements RecordAccessor, BatchRecordAccessor {

    // Just one mapping context is shared by all instances. It is thread-safe and configured the same every time. There
    // is no reason to go through the expense of creating multiple instances. This way we get to share the cache.
    private static final ObjectMappingContext mappingContext = new ObjectMappingContext();

    private final RestConnector connector;

    public RestRecordAccessor(RestConnector connector) {
        this.connector = connector;
    }

    @Override
    public void execute(RecordOperation<?> operation) {
        RestRecordOperationExecutor executor = new RestRecordOperationExecutor(connector, mappingContext);
        operation.execute(executor);
        connector.flush();
    }

    @Override
    public void execute(List<RecordOperation<?>> operations) {
        RestConnector batchedConnector = new BatchedRestConnector(connector);
        RestRecordOperationExecutor executor = new RestRecordOperationExecutor(batchedConnector, mappingContext);

        for (RecordOperation<?> operation : operations)
            operation.execute(executor);

        batchedConnector.flush();
    }

    @Override
    public String create(Object record) {
        CreateRecordOperation operation = new CreateRecordOperation(record);
        execute(operation);
        if (operation.isSuccessful()) {
            return operation.getResult();
        } else {
            throw operation.getException();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String id, Class<T> recordClass) {
        GetRecordOperation operation = new GetRecordOperation<T>(id, recordClass);
        execute(operation);
        if (operation.isSuccessful()) {
            return (T) operation.getResult();
        } else {
            throw operation.getException();
        }
    }

    @Override
    public void update(Object record) {
        Validate.notNull(record, "record must not be null");

        update(getRecordId(record), record);
    }

    @Override
    public void update(String id, Object record) {
        UpdateRecordOperation operation = new UpdateRecordOperation(id, record);
        execute(operation);
        if (!operation.isSuccessful()) {
            throw operation.getException();
        }
    }

    @Override
    public void patch(String id, Object recordChanges) {
        PatchRecordOperation operation = new PatchRecordOperation(id, recordChanges);
        execute(operation);
        if (!operation.isSuccessful()) {
            throw operation.getException();
        }
    }

    @Override
    public void delete(Object record) {
        Validate.notNull(record, "record must not be null");

        delete(getRecordId(record), record.getClass());
    }

    @Override
    public void delete(String id, Class<?> recordClass) {
        DeleteRecordOperation operation = new DeleteRecordOperation(id, recordClass);
        execute(operation);
        if (!operation.isSuccessful()) {
            throw operation.getException();
        }
    }

    @Override
    public <T> RecordQuery<T> createQuery(final String soqlTemplate, final Class<T> recordClass) {
        Validate.notNull(soqlTemplate, "soqlTemplate must not be null");
        Validate.notNull(recordClass, "recordClass must not be null");

        return new RestRecordQuery<T>(soqlTemplate, recordClass);
    }

    private final class RestRecordQuery<T> extends AbstractRecordQuery<T> {
        private Class<T> recordClass;
        private String soqlTemplate;

        private RestRecordQuery(String soqlTemplate, Class<T> recordClass) {
            this.recordClass = recordClass;
            this.soqlTemplate = soqlTemplate;
        }

        @Override
        public List<T> execute() {
            return execute(recordClass);
        }

        @Override
        public <R> List<R> execute(Class<R> resultClass) {
            QueryRecordsOperation<R> operation = new QueryRecordsOperation<R>(soqlTemplate, recordClass, resultClass);
            RestRecordAccessor.this.execute(operation);
            if (operation.isSuccessful()) {
                return operation.getResult();
            } else {
                throw operation.getException();
            }
        }
    }

    /**
     * Get the "id" field of a record.
     * <p/>
     * This would normally be private but is made public to help with bridging the old Simplejpa interface to this new
     * interface.
     *
     * @param record the record
     */
    public String getRecordId(Object record) {
        ObjectDescriptor descriptor = mappingContext.getRequiredObjectDescriptor(record.getClass());
        if (descriptor.hasIdMember()) {
            String id = RecordUtils.getId(descriptor, record);
            if (StringUtils.isEmpty(id)) {
                throw new RecordRequestException("Record bean does not have an id value set");
            }
            return id;
        } else {
            throw new RecordRequestException("Record class doesn't have an id member");
        }
    }
}
