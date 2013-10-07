/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.util.List;

public class QueryResult {

    private boolean done;

    private int totalSize;

    private String nextRecordsUrl;

    private List<Object> records;

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public String getNextRecordsUrl() {
        return nextRecordsUrl;
    }

    public void setNextRecordsUrl(String nextRecordsUrl) {
        this.nextRecordsUrl = nextRecordsUrl;
    }

    public List<Object> getRecords() {
        return records;
    }

    public void setRecords(List<Object> records) {
        this.records = records;
    }
}
