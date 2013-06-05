/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.util.concurrent.ExecutionException;

/**
 * @param <R> the type of result expected from the operation
 */
public interface RecordOperation<R> {
    /**
     * Returns an indication of whether the operation is done. Completion could be the result of successful execution or
     * an exception.
     */
    boolean isDone();

    /**
     * Returns the operation result.
     *
     * @return the operation result
     * @throws ExecutionException if the operation threw an exception
     */
    R get() throws ExecutionException;
}
