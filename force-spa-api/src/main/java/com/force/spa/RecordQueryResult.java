/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.util.List;

/**
 * An interface for accessing the results of a SOQL query.
 *
 * @param <T> the type of result records
 */
public interface RecordQueryResult<T> {

    /**
     * Returns total number of records that satisfied by the query. This number may be larger than number of records
     * returned by {@link #getRecords} if the query specified a maximum number of records to return.
     *
     * @return the total number of records that satisfied by the query
     */
    int getTotalSize();

    /**
     * Indicates whether all the requested records are in this result.
     * <p/>
     * Currently this always returns "true".
     *
     * @return an indication of whether all the requested records are in this result
     */
    boolean isDone();

    /**
     * Returns the list of records.
     *
     * @return the list of records
     */
    List<T> getRecords();
}
