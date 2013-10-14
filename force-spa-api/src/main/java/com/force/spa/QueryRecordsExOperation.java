/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.util.List;

/**
 * @param <T> the type of record the operation is working with
 * @param <R> the type of result expected from the operation
 */
public interface QueryRecordsExOperation<T, R> extends RecordOperation<List<R>> {
    String getSoqlTemplate();

    Class<T> getRecordClass();

    Class<R> getResultClass();

    void setStartPosition(int offset);

    int getStartPosition();

    void setMaxResults(int limit);

    int getMaxResults();
}
