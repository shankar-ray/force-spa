/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.batch;

public final class UpdateRecordOperation extends RecordOperation<Void> {
    private final String id;
    private final Object record;

    public UpdateRecordOperation(String id, Object record) {
        if (id == null)
            throw new IllegalArgumentException("id must not be null");
        if (record == null)
            throw new IllegalArgumentException("record must not be null");

        this.id = id;
        this.record = record;
    }

    public String getId() {
        return id;
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
