/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.batch;

import java.util.List;

public final class QueryRecordsOperation<T> extends RecordOperation<List<T>> {
    private final String soqlTemplate;
    private final Class<?> recordClass;
    private final Class<T> resultClass;

//TODO need to deal with offset and limit stuff. What is maximum batch size supported by generic rest so we can avoid internal paging?

    public QueryRecordsOperation(String soql, Class<T> recordClass) {
        this(soql, recordClass, recordClass);
    }

    public QueryRecordsOperation(String soqlTemplate, Class<?> recordClass, Class<T> resultClass) {
        if (soqlTemplate == null)
            throw new IllegalArgumentException("soqlTemplate must not be null");
        if (recordClass == null)
            throw new IllegalArgumentException("recordClass must not be null");
        if (resultClass == null)
            throw new IllegalArgumentException("resultClass must not be null");

        this.soqlTemplate = soqlTemplate;
        this.resultClass = resultClass;
        this.recordClass = recordClass;
    }

    public String getSoqlTemplate() {
        return soqlTemplate;
    }

    public Class<?> getRecordClass() {
        return recordClass;
    }

    public Class<T> getResultClass() {
        return resultClass;
    }

    @Override
    public void execute(RecordOperationVisitor executor) {
        setStarted();
        executor.visit(this);
    }
}
