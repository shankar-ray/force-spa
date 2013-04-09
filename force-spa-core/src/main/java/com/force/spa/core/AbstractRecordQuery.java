/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.force.spa.RecordQuery;

/**
 * An abstract implementation of {@link RecordQuery} that handles some standard stuff so that derived classes just need
 * to worry about implementing {@link RecordQuery#execute()}.
 *
 * @param <T> the class of returned records
 */
abstract class AbstractRecordQuery<T> implements RecordQuery<T> {
    private int maxResults;
    private int startPosition;

    @Override
    public RecordQuery<T> setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    protected int getMaxResults() {
        return maxResults;
    }

    @Override
    public RecordQuery<T> setFirstResult(int startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    protected int getFirstResult() {
        return startPosition;
    }
}
