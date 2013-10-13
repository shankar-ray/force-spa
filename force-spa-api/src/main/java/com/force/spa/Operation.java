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
public interface Operation<R> {

    /**
     * Returns the operation result.
     *
     * @return the operation result
     * @throws ExecutionException if the operation threw an exception. Interesting 'cause' values for the execution
     * exception include {@link UnauthorizedException}, {@link ObjectNotFoundException}, {@link RecordNotFoundException}
     */
    R get() throws ExecutionException;

    /**
     * Returns an indication of whether the operation is completed. Completion could be the result of successful
     * execution or an exception.
     */
    boolean isCompleted();

    /**
     * Returns an indication of whether the operation is batched with other operations.
     */
    boolean isBatched();

    /**
     * Returns statistics for the operation execution. The information is only available after the operation has
     * completed (or failed).
     */
    OperationStatistics getStatistics();
}
