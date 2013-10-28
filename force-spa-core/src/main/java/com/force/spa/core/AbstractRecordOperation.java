/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.force.spa.RecordOperation;
import com.force.spa.Statistics;

/**
 * @param <T> the type of record the operation is working with
 * @param <R> the type of result expected from the operation
 */
public abstract class AbstractRecordOperation<T, R> implements RecordOperation<R>, CompletionHandler<R, Statistics> {

    private static final String STATISTICS_MDC_KEY = "spa.statistics";
    private static final String STATISTICS_SIMPLE_MDC_KEY = "spa.statistics.simple";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AbstractRecordAccessor recordAccessor;
    private final ObjectDescriptor objectDescriptor;

    private R result;
    private Throwable exception;
    private boolean done;
    private boolean cancelled;
    private boolean batched;
    private Statistics statistics;

    protected AbstractRecordOperation(AbstractRecordAccessor recordAccessor, Class<T> recordClass) {
        this.recordAccessor = recordAccessor;
        this.done = false;
        this.cancelled = false;
        this.batched = false;

        // Get the descriptor at early to make sure mapping context is loaded and ready to handle inbound polymorphism.
        this.objectDescriptor = (recordClass != null) ? getMappingContext().getObjectDescriptor(recordClass) : null;
    }

    @Override
    public final boolean isDone() {
        return done;
    }

    @Override
    public final boolean isCancelled() {
        return cancelled;
    }

    @Override
    public final boolean isBatched() {
        return batched;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    @Override
    public final R get() throws ExecutionException {
        if (done) {
            if (exception != null) {
                throw new ExecutionException(exception);
            } else {
                return result;
            }
        } else {
            throw new IllegalStateException("Operation is not done yet");
        }
    }

    @Override
    public void completed(R result, Statistics statistics) {
        if (done) {
            throw new IllegalStateException("Operation is already done");
        }
        done = true;
        this.result = result;
        this.statistics = statistics;

        if (getLogger().isInfoEnabled()) {
            MDC.put(STATISTICS_MDC_KEY, statistics.toString(KeyValueToStringStyle.INSTANCE));
            MDC.put(STATISTICS_SIMPLE_MDC_KEY, statistics.toString(ToStringStyle.SIMPLE_STYLE));
            getLogger().info((isBatched() ? "(Batched) " : "") + this);
            MDC.remove(STATISTICS_SIMPLE_MDC_KEY);
            MDC.remove(STATISTICS_MDC_KEY);
        }
    }

    @Override
    public void failed(Throwable exception, Statistics statistics) {
        if (done) {
            throw new IllegalStateException("Operation is already done");
        }
        done = true;
        this.exception = exception;
        this.statistics = statistics;

        if (getLogger().isInfoEnabled()) {
            MDC.put(STATISTICS_MDC_KEY, statistics.toString(KeyValueToStringStyle.INSTANCE));
            MDC.put(STATISTICS_SIMPLE_MDC_KEY, statistics.toString(ToStringStyle.SIMPLE_STYLE));
            getLogger().info((isBatched() ? "(Batched) " : "") + this, exception);
            MDC.remove(STATISTICS_SIMPLE_MDC_KEY);
            MDC.remove(STATISTICS_MDC_KEY);
        }
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    public AbstractRecordAccessor getRecordAccessor() {
        return recordAccessor;
    }

    public final ObjectDescriptor getObjectDescriptor() {
        return objectDescriptor;
    }

    public final void setBatched(boolean batched) {
        this.batched = batched;
    }

    protected final MappingContext getMappingContext() {
        return recordAccessor.getMappingContext();
    }

    protected final Logger getLogger() {
        return logger;
    }

    private static final class KeyValueToStringStyle extends ToStringStyle {
        private static final long serialVersionUID = 9179813885104212954L;

        static final KeyValueToStringStyle INSTANCE = new KeyValueToStringStyle();

        private KeyValueToStringStyle() {
            super();
            this.setUseClassName(false);
            this.setUseIdentityHashCode(false);
            this.setUseFieldNames(true);
            this.setContentStart("");
            this.setContentEnd("");
        }

        @SuppressWarnings("SameReturnValue")
        private Object readResolve() {
            return INSTANCE; // Ensure singleton after serialization
        }
    }
}
