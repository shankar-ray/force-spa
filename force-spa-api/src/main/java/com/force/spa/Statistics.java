/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Simple statistics for a SPA operation.
 */
public class Statistics implements Serializable {

    private static final ToStringStyle TO_STRING_STYLE = new OperationStatisticsToStringStyle();

    private static final long serialVersionUID = 5658021718579655518L;

    private final long bytesSent;
    private final long bytesReceived;
    private final long elapsedNanos;
    private final long rowsProcessed;
    private final long totalRows;

    protected Statistics(Builder builder) {
        this.bytesSent = builder.bytesSent;
        this.bytesReceived = builder.bytesReceived;
        this.elapsedNanos = builder.elapsedNanos;
        this.rowsProcessed = builder.rowsProcessed;
        this.totalRows = builder.totalRows;
    }

    /**
     * Returns the number of data bytes sent to the server.
     *
     * @return the number of data bytes sent to the server
     */
    public final long getBytesSent() {
        return bytesSent;
    }

    /**
     * Returns the number of data bytes received from the server.
     *
     * @return the number of data bytes received from the server
     */
    public final long getBytesReceived() {
        return bytesReceived;
    }

    /**
     * Returns the number of nanoseconds elapsed during operation processing.
     *
     * @return the number of nanoseconds elapsed during operation processing
     */
    public final long getElapsedNanos() {
        return elapsedNanos;
    }

    /**
     * Returns the number of rows that were processed.
     *
     * @return the number of rows that were processed
     */
    public final long getRowsProcessed() {
        return rowsProcessed;
    }

    /**
     * Returns the total number of rows that satisfied the query.
     *
     * @return the total number of rows that satisfied the query
     */
    public final long getTotalRows() {
        return totalRows;
    }

    @Override
    public final String toString() {
        return toString(TO_STRING_STYLE);
    }

    public final String toString(ToStringStyle style) {
        return appendToStringBuilder(new ToStringBuilder(this, style)).toString();
    }

    protected ToStringBuilder appendToStringBuilder(ToStringBuilder builder) {
        builder.append("millis", NANOSECONDS.toMicros(elapsedNanos) / 1000D); // Convert to floating point millis
        builder.append("sent", bytesSent);
        builder.append("received", bytesReceived);
        builder.append("rows", rowsProcessed);
        builder.append("totalRows", totalRows);

        return builder;
    }

    public static class Builder {

        private long bytesSent;
        private long bytesReceived;
        private long elapsedNanos;
        private long rowsProcessed;
        private long totalRows;

        public Builder() {
            bytesSent = 0;
            bytesReceived = 0;
            elapsedNanos = 0;
            rowsProcessed = 0;
            totalRows = 0;
        }

        public Builder(Statistics that) {
            this.bytesSent = that.bytesSent;
            this.bytesReceived = that.bytesReceived;
            this.elapsedNanos = that.elapsedNanos;
            this.rowsProcessed = that.rowsProcessed;
            this.totalRows = that.totalRows;
        }

        public Statistics build() {
            return new Statistics(this);
        }

        public Builder bytesSent(long bytesSent) {
            this.bytesSent = bytesSent;
            return this;
        }

        public Builder bytesReceived(long bytesReceived) {
            this.bytesReceived = bytesReceived;
            return this;
        }

        public Builder elapsedNanos(long elapsedNanos) {
            this.elapsedNanos = elapsedNanos;
            return this;
        }

        public Builder rowsProcessed(long rowsProcessed) {
            this.rowsProcessed = rowsProcessed;
            return this;
        }

        public Builder totalRows(long totalRows) {
            this.totalRows = totalRows;
            return this;
        }

        public Builder additionalBytesSent(long additionalBytesSent) {
            this.bytesSent += additionalBytesSent;
            return this;
        }

        public Builder additionalBytesReceived(long additionalBytesReceived) {
            this.bytesReceived = additionalBytesReceived;
            return this;
        }

        public Builder additionalElapsedNanos(long additionalElapsedNanos) {
            this.elapsedNanos = additionalElapsedNanos;
            return this;
        }

        public Builder additionalRowsProcessed(long additionalRowsProcessed) {
            this.rowsProcessed = additionalRowsProcessed;
            return this;
        }

        public Builder additionalTotalRows(long additionalTotalRows) {
            this.totalRows += additionalTotalRows;
            return this;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }
    }

    public static class OperationStatisticsToStringStyle extends ToStringStyle {
        private static final long serialVersionUID = -8799177462827453155L;

        OperationStatisticsToStringStyle() {
            super();
            this.setUseClassName(false);
            this.setUseIdentityHashCode(false);
            this.setUseFieldNames(true);
        }

        @SuppressWarnings("SameReturnValue")
        private Object readResolve() {
            return Statistics.TO_STRING_STYLE; // Ensure singleton after serialization
        }
    }
}
