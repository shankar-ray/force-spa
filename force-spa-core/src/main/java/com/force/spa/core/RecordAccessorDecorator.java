/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.List;

import com.force.spa.CreateRecordOperation;
import com.force.spa.DeleteRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordOperation;
import com.force.spa.RecordQuery;
import com.force.spa.UpdateRecordOperation;

/**
 * A convenience class for {@link RecordAccessor} decorators. This abstract base class delegates all of the {@link
 * RecordAccessor} methods so that specific decorators implementations only need to worry about overriding the classes
 * they are interested in. For decorators that are only interested in a small number of methods extending this class
 * helps provide isolation from additions to the record accessor interface.
 * <p/>
 * This class also provides important semantic protection by marking methods final that should generally not be
 * decorated. If the method is final in {@link AbstractRecordAccessor} then it is marked final here as well. This is
 * because  proper batching semantics rely upon a common execution funnel. You should be able to achieve your goal by
 * only decorating the remaining, non-final, methods.
 */
public abstract class RecordAccessorDecorator implements RecordAccessor {

    private final RecordAccessor delegate;

    protected RecordAccessorDecorator(RecordAccessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(List<RecordOperation<?>> operations) {
        delegate.execute(operations);
    }

    @Override
    public <T> CreateRecordOperation<T> newCreateRecordOperation(T record) {
        return delegate.newCreateRecordOperation(record);
    }

    @Override
    public <T> DeleteRecordOperation<T> newDeleteRecordOperation(String id, Class<T> recordClass) {
        return delegate.newDeleteRecordOperation(id, recordClass);
    }

    @Override
    public <T> GetRecordOperation<T> newGetRecordOperation(String id, Class<T> recordClass) {
        return delegate.newGetRecordOperation(id, recordClass);
    }

    @Override
    public <T> PatchRecordOperation<T> newPatchRecordOperation(String id, T recordChanges) {
        return delegate.newPatchRecordOperation(id, recordChanges);
    }

    @Override
    public <T> QueryRecordsOperation<T, T> newQueryRecordsOperation(String soqlTemplate, Class<T> recordClass) {
        return delegate.newQueryRecordsOperation(soqlTemplate, recordClass);
    }

    @Override
    public <T, R> QueryRecordsOperation<T, R> newQueryRecordsOperation(String soqlTemplate, Class<T> recordClass, Class<R> resultClass) {
        return delegate.newQueryRecordsOperation(soqlTemplate, recordClass, resultClass);
    }

    @Override
    public <T> UpdateRecordOperation<T> newUpdateRecordOperation(String id, T record) {
        return delegate.newUpdateRecordOperation(id, record);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

// ***** Starting here are things you should not decorate (or need to decorate). *****

    @Override
    public final <T> String create(T record) {
        return delegate.create(record);
    }

    @Override
    public final <T> RecordQuery<T> createQuery(String soqlTemplate, Class<T> recordClass) {
        return delegate.createQuery(soqlTemplate, recordClass);
    }

    @Override
    public final <T> void delete(String id, Class<T> recordClass) {
        delegate.delete(id, recordClass);
    }

    @Override
    public final <T> void delete(T record) {
        delegate.delete(record);
    }

    @Override
    public final void execute(RecordOperation<?>... operations) {
        delegate.execute(operations);
    }

    @Override
    public final <T> T get(String id, Class<T> recordClass) {
        return delegate.get(id, recordClass);
    }

    @Override
    public final <T> void patch(String id, T recordChanges) {
        delegate.patch(id, recordChanges);
    }

    @Override
    public final <T> void update(String id, T record) {
        delegate.update(id, record);
    }

    @Override
    public final <T> void update(T record) {
        delegate.update(record);
    }

    @Override
    public final RecordAccessorConfig getConfig() {
        return delegate.getConfig();
    }
}
