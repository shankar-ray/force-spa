/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.batch;

public final class GetRecordOperation<T> extends RecordOperation<T> {
    private final String id;
    private final Class<T> recordClass;

    public GetRecordOperation(String id, Class<T> recordClass) {
        if (id == null)
            throw new IllegalArgumentException("id must not be null");
        if (recordClass == null)
            throw new IllegalArgumentException("recordClass must not be null");

        this.id = id;
        this.recordClass = recordClass;
    }

    public String getId() {
        return id;
    }

    public Class<T> getRecordClass() {
        return recordClass;
    }

    @Override
    public void execute(RecordOperationVisitor executor) {
        setStarted();
        executor.visit(this);
    }
}
