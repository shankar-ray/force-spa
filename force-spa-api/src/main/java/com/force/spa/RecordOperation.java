/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.util.concurrent.Future;

/**
 * @param <R> the type of result expected from the operation
 */
public interface RecordOperation<R> extends Future<R> {
    /**
     * Returns statistics for the operation execution. The information is only available after the operation is
     * done (successfully or not).
     */
    Statistics getStatistics();
}
