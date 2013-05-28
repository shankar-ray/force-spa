/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.batch;

public final class CreateRecordOperation extends RecordOperation<String> {
    private final Object record;

    public CreateRecordOperation(Object record) {
        if (record == null)
            throw new IllegalArgumentException("record must not be null");

        this.record = record;
    }

    public Object getRecord() {
        return record;
    }

    @Override
    public void execute(RecordOperationVisitor executor) {
        setStarted();
        executor.visit(this);
    }
}
