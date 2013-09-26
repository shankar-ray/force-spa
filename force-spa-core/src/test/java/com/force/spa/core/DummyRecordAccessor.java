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
import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordOperation;
import com.force.spa.UpdateRecordOperation;

/**
 * A dummy implementation of {@link com.force.spa.RecordAccessor} to help with unit tests.
 * <p/>
 * This initial implementation is not fully realized but as testing needs grow we can implement more and more.
 */
public class DummyRecordAccessor extends AbstractRecordAccessor {

    public DummyRecordAccessor() {
        this(new RecordAccessorConfig());
    }

    public DummyRecordAccessor(RecordAccessorConfig config) {
        super(config, new DummyMetadataAccessor(config));
    }

    @Override
    public void execute(List<RecordOperation<?>> operations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> CreateRecordOperation<T> newCreateRecordOperation(T record) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> DeleteRecordOperation<T> newDeleteRecordOperation(String id, Class<T> recordClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> GetRecordOperation<T> newGetRecordOperation(String id, Class<T> recordClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> PatchRecordOperation<T> newPatchRecordOperation(String id, T recordChanges) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> UpdateRecordOperation<T> newUpdateRecordOperation(String id, T record) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soqlTemplate, Class<T> recordClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soqlTemplate, Class<?> recordClass, Class<T> resultClass) {
        throw new UnsupportedOperationException();
    }
}
