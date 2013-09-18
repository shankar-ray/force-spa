/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.Collections;
import java.util.List;

import com.force.spa.RecordQueryResult;

public class SimpleRecordQueryResult<T> implements RecordQueryResult<T> {

    private final int totalSize;
    private final boolean done;
    private final List<T> records;

    public SimpleRecordQueryResult(int totalSize, boolean done, List<T> records) {
        this.totalSize = totalSize;
        this.done = done;
        this.records = Collections.unmodifiableList(records);
    }

    public boolean isDone() {
        return done;
    }

    public List<T> getRecords() {
        return records;
    }

    public int getTotalSize() {
        return totalSize;
    }
}
