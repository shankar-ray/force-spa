/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class QueryRecordsStatistics extends OperationStatistics {

    private final long totalRows;
    private final long rowsProcessed;

    protected QueryRecordsStatistics(Builder builder) {
        super(builder);
        this.totalRows = builder.totalRows;
        this.rowsProcessed = builder.rowsProcessed;
    }

    /**
     * Returns the total number of rows that satisfied the query.
     *
     * @return the total number of rows that satisfied the query
     */
    public final long getTotalRows() {
        return totalRows;
    }

    /**
     * Returns the number of rows that were processed.
     *
     * @return the number of rows that were processed
     */
    public final long getRowsProcessed() {
        return rowsProcessed;
    }

    public static class Builder extends OperationStatistics.Builder {

        private long totalRows;
        private long rowsProcessed;

        public Builder() {
            totalRows = 0;
            rowsProcessed = 0;
        }

        public Builder(OperationStatistics that) {
            super(that);
        }

        public Builder(QueryRecordsStatistics that) {
            super(that);
            this.totalRows = that.totalRows;
            this.rowsProcessed = that.rowsProcessed;
        }

        @Override
        public QueryRecordsStatistics build() {
            return new QueryRecordsStatistics(this);
        }

        @Override
        public Builder bytesReceived(long bytesReceived) {
            return (Builder) super.bytesReceived(bytesReceived);
        }

        @Override
        public Builder bytesSent(long bytesSent) {
            return (Builder) super.bytesSent(bytesSent);
        }

        @Override
        public Builder elapsedNanos(long elapsedNanos) {
            return (Builder) super.elapsedNanos(elapsedNanos);
        }

        public Builder totalRows(long totalRows) {
            this.totalRows = totalRows;
            return this;
        }

        public Builder rowsProcessed(long rowsProcessed) {
            this.rowsProcessed = rowsProcessed;
            return this;
        }

        @Override
        public Builder additionalBytesReceived(long additionalBytesReceived) {
            return (Builder) super.additionalBytesReceived(additionalBytesReceived);
        }

        @Override
        public Builder additionalBytesSent(long additionalBytesSent) {
            return (Builder) super.additionalBytesSent(additionalBytesSent);
        }

        @Override
        public Builder additionalElapsedNanos(long additionalElapsedNanos) {
            return (Builder) super.additionalElapsedNanos(additionalElapsedNanos);
        }

        public Builder additionalTotalRows(long additionalTotalRows) {
            this.totalRows += additionalTotalRows;
            return this;
        }

        public Builder additionalRowsProcessed(long additionalRowsProcessed) {
            this.rowsProcessed = additionalRowsProcessed;
            return this;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }
    }
}
