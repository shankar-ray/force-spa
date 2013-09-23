/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.force.spa.CreateRecordOperation;
import com.force.spa.DeleteRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.MetadataAccessor;
import com.force.spa.PatchRecordOperation;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordOperation;
import com.force.spa.RecordQuery;
import com.force.spa.RecordRequestException;
import com.force.spa.UpdateRecordOperation;

public abstract class AbstractRecordAccessor implements RecordAccessor {

    private static ObjectMappingContextCache objectMappingContextCache = new ObjectMappingContextCache();

    private final RecordAccessorConfig config;
    private final MetadataAccessor metadataAccessor;
    private final ObjectMappingContext mappingContext;

    protected AbstractRecordAccessor(RecordAccessorConfig config, MetadataAccessor metadataAccessor) {
        this.config = config;
        this.metadataAccessor = metadataAccessor;
        this.mappingContext = objectMappingContextCache.getObjectMappingContext(config);
    }

    /**
     * Executes a single record operation. This is used internally and is the primary mechanism for specific record
     * accessor implementations to leverage common code in this base class.
     */
    protected abstract void execute(RecordOperation<?> operation);

    @Override
    public void execute(RecordOperation<?>... operations) {
        if (operations.length == 1) {
            execute(operations[0]); // Optimize for the single operation case
        } else {
            execute(Arrays.asList(operations));
        }
    }

    @Override
    public final <T> String create(T record) {
        CreateRecordOperation<T> operation = newCreateRecordOperation(record);
        execute(operation);
        try {
            return operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final <T> T get(String id, Class<T> type) {
        GetRecordOperation<T> operation = newGetRecordOperation(id, type);
        execute(operation);
        try {
            return operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final <T> void update(T record) {
        Validate.notNull(record, "record must not be null");

        update(getRecordId(record), record);
    }

    @Override
    public final <T> void update(String id, T record) {
        UpdateRecordOperation<T> operation = newUpdateRecordOperation(id, record);
        execute(operation);
        try {
            operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final <T> void patch(String id, T recordChanges) {
        PatchRecordOperation<T> operation = newPatchRecordOperation(id, recordChanges);
        execute(operation);
        try {
            operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final <T> void delete(T record) {
        Validate.notNull(record, "record must not be null");

        delete(getRecordId(record), record.getClass());
    }

    @Override
    public final <T> void delete(String id, Class<T> type) {
        DeleteRecordOperation<T> operation = newDeleteRecordOperation(id, type);
        execute(operation);
        try {
            operation.get();
        } catch (ExecutionException e) {
            throw getCauseAsRuntimeException(e);
        }
    }

    @Override
    public final <T> RecordQuery<T> createQuery(final String soqlTemplate, final Class<T> type) {
        Validate.notNull(soqlTemplate, "template must not be null");
        Validate.notNull(type, "type must not be null");

        return new RecordQueryImpl<T>(soqlTemplate, type);
    }

    @Override
    public final RecordAccessorConfig getConfig() {
        return config;
    }

    @Override
    public final MetadataAccessor getMetadataAccessor() {
        return metadataAccessor;
    }

//TODO This may be only awkward cross reference left.
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
        ObjectDescriptor object = mappingContext.getRequiredObjectDescriptor(record.getClass());
        if (object.hasIdField()) {
            String id = object.getIdField().getValue(record);
            if (StringUtils.isEmpty(id)) {
                throw new RecordRequestException("Record bean does not have an id value set");
            }
            return id;
        } else {
            throw new RecordRequestException("Record class doesn't have an id member");
        }
    }

    private final class RecordQueryImpl<T> implements RecordQuery<T> {
        private final Class<T> type;
        private final String soqlTemplate;
        private int maxResults;
        private int startPosition;


        private RecordQueryImpl(String soqlTemplate, Class<T> type) {
            this.type = type;
            this.soqlTemplate = soqlTemplate;
        }

        @Override
        public List<T> execute() {
            return execute(type);
        }

        @Override
        public <R> List<R> execute(Class<R> resultClass) {
            QueryRecordsOperation<R> operation = newQueryRecordsOperation(soqlTemplate, type, resultClass);
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
