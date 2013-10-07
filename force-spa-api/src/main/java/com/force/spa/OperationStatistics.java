/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.text.DecimalFormat;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Simple statistics for an operation execution.
 */
public class OperationStatistics {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###,###µs");

    private final long bytesSent;
    private final long bytesReceived;
    private final long elapsedNanos;

    protected OperationStatistics(Builder builder) {
        this.bytesSent = builder.bytesSent;
        this.bytesReceived = builder.bytesReceived;
        this.elapsedNanos = builder.elapsedNanos;
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
     * Returns the number of microseconds elapsed during operation processing.
     *
     * @return the number of microseconds elapsed during operation processing
     */
    public final long getElapsedMicros() {
        return NANOSECONDS.toMicros(elapsedNanos);
    }

    @Override
    public String toString() {
        String formattedElapsedTime = DECIMAL_FORMAT.format(getElapsedMicros());
        StringBuilder builder = new StringBuilder();
        builder.append("{bytesSent=").append(bytesSent);
        builder.append(", bytesReceived=").append(bytesReceived);
        builder.append(", elapsedTime=").append(formattedElapsedTime);
        return builder.toString();
    }

    public static class Builder {

        private long bytesSent;
        private long bytesReceived;
        private long elapsedNanos;

        public Builder() {
            bytesSent = 0;
            bytesReceived = 0;
            elapsedNanos = 0;
        }

        public Builder(OperationStatistics that) {
            this.bytesSent = that.bytesSent;
            this.bytesReceived = that.bytesReceived;
            this.elapsedNanos = that.elapsedNanos;
        }

        public OperationStatistics build() {
            return new OperationStatistics(this);
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

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }
    }
}
