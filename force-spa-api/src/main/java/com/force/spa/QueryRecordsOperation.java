/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

public interface QueryRecordsOperation<T> extends RecordOperation<RecordQueryResult<T>> {
    String getSoqlTemplate();

    Class<?> getRecordClass();

    Class<T> getResultClass();

    void setStartPosition(int offset);

    int getStartPosition();

    void setMaxResults(int limit);

    int getMaxResults();
}
