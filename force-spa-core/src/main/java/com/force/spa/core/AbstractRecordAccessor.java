/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.force.spa.CreateRecordOperation;
import com.force.spa.DeleteRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordQuery;
import com.force.spa.RecordRequestException;
import com.force.spa.UpdateRecordOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class AbstractRecordAccessor implements RecordAccessor {

    // Just one mapping context is shared by all instances. It is thread-safe and configured the same every time. There
    // is no reason to go through the expense of creating multiple instances. This way we get to share the cache.
    private static final ObjectMappingContext mappingContext = new ObjectMappingContext();

    @Override
    public final String create(Object record) {
        CreateRecordOperation operation = newCreateRecordOperation(record);
        execute(operation);
        try {
            return operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final <T> T get(String id, Class<T> recordClass) {
        GetRecordOperation<T> operation = newGetRecordOperation(id, recordClass);
        execute(operation);
        try {
            return operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final void update(Object record) {
        Validate.notNull(record, "record must not be null");

        update(getRecordId(record), record);
    }

    @Override
    public final void update(String id, Object record) {
        UpdateRecordOperation operation = newUpdateRecordOperation(id, record);
        execute(operation);
        try {
            operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final void patch(String id, Object recordChanges) {
        PatchRecordOperation operation = newPatchRecordOperation(id, recordChanges);
        execute(operation);
        try {
            operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final void delete(Object record) {
        Validate.notNull(record, "record must not be null");

        delete(getRecordId(record), record.getClass());
    }

    @Override
    public final void delete(String id, Class<?> recordClass) {
        DeleteRecordOperation operation = newDeleteRecordOperation(id, recordClass);
        execute(operation);
        try {
            operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final <T> RecordQuery<T> createQuery(final String soqlTemplate, final Class<T> recordClass) {
        Validate.notNull(soqlTemplate, "soqlTemplate must not be null");
        Validate.notNull(recordClass, "recordClass must not be null");

        return new RestRecordQuery<T>(soqlTemplate, recordClass);
    }

    protected final ObjectMappingContext getMappingContext() {
        return mappingContext;
    }

    private static RuntimeException getCauseAsRuntimeException(ExecutionException e) {
        if (e.getCause() instanceof RuntimeException)
            return (RuntimeException) e.getCause();
        else
            return new RuntimeException(e.getCause());
    }

    private String getRecordId(Object record) {
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

    private final class RestRecordQuery<T> implements RecordQuery<T> {
        private final Class<T> recordClass;
        private final String soqlTemplate;
        private int maxResults;
        private int startPosition;


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
            QueryRecordsOperation<R> operation = newQueryRecordsOperation(soqlTemplate, recordClass, resultClass);
            if (startPosition != 0)
                operation.setStartPosition(startPosition);
            if (maxResults != 0)
                operation.setMaxResults(maxResults);
            AbstractRecordAccessor.this.execute(operation);
            try {
                return operation.get();
            } catch (ExecutionException e) {
                throw getCauseAsRuntimeException(e);
            }
        }

        @Override
        public RecordQuery<T> setMaxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        @Override
        public RecordQuery<T> setFirstResult(int startPosition) {
            this.startPosition = startPosition;
            return this;
        }
    }
}
