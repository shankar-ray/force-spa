/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.util.List;

/**
 * @param <T> the type of record the operation is working with
 */
public interface QueryRecordsOperation<T> extends RecordOperation<List<T>> {
    String getSoqlTemplate();

    Class<T> getResultClass();

    void setStartPosition(int offset);

    int getStartPosition();

    void setMaxResults(int limit);

    int getMaxResults();
}
