/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.batch;

public final class DeleteRecordOperation extends RecordOperation<Void> {
    private final String id;
    private final Class<?> recordClass;

    public DeleteRecordOperation(String id, Class<?> recordClass) {
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

    public Class<?> getRecordClass() {
        return recordClass;
    }

    @Override
    public void execute(RecordOperationVisitor executor) {
        setStarted();
        executor.visit(this);
    }
}
