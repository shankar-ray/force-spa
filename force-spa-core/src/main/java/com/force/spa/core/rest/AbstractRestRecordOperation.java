/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import com.force.spa.Statistics;
import com.force.spa.core.AbstractRecordOperation;

/**
 * @param <T> the type of record the operation is working with
 * @param <R> the type of result expected from the operation
 */
public abstract class AbstractRestRecordOperation<T, R> extends AbstractRecordOperation<T, R> {

    protected AbstractRestRecordOperation(RestRecordAccessor accessor, Class<T> recordClass) {
        super(accessor, recordClass);
    }

    @Override
    public RestRecordAccessor getRecordAccessor() {
        return (RestRecordAccessor) super.getRecordAccessor();
    }

    /**
     * Starts the (potentially asynchronous) operation.
     *
     * @param connector a REST connector
     */
    protected abstract void start(RestConnector connector);

    /**
     * A default REST response handler with many methods implemented appropriately for an all operations. Individual
     * operations only need to override those things they need handle to do differently.
     */
    protected class ResponseHandler extends RestResponseHandler<R> {
        @Override
        public void completed(R result, Statistics statistics) {
            AbstractRestRecordOperation.this.completed(result, statistics);
        }

        @Override
        public void failed(Throwable exception, Statistics statistics) {
            AbstractRestRecordOperation.this.failed(exception, statistics);
        }
    }
}
