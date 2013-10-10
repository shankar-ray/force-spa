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

    String STATISTICS_MDC_KEY = "spa.statistics";

    /**
     * Returns the operation result.
     *
     * @return the operation result
     * @throws ExecutionException if the operation threw an exception
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
     * Returns a title for the operation that can be used in logging or diagnostics messages.
     */
    String getTitle();

    /**
     * Returns a piece of detailed operation information that can be used in logging or diagnostics messages. You should
     * probably only display this value when debugging because the value some often be very large.
     */
    Object getDetail();

    /**
     * Returns statistics for the operation execution. The information is only available after the operation has
     * completed (or failed).
     */
    OperationStatistics getStatistics();
}
