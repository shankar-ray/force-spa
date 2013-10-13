/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.force.spa.Operation;
import com.force.spa.OperationStatistics;

/**
 * @param <T> the type of record the operation is working with
 * @param <R> the type of result expected from the operation
 */
public abstract class AbstractOperation<T, R> implements Operation<R>, CompletionHandler<R, OperationStatistics> {

    private static final String STATISTICS_MDC_KEY = "spa.statistics";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AbstractRecordAccessor recordAccessor;
    private final ObjectDescriptor objectDescriptor;

    private R result;
    private Throwable exception;
    private boolean completed;
    private boolean batched;
    private OperationStatistics statistics;

    protected AbstractOperation(AbstractRecordAccessor recordAccessor, Class<T> recordClass) {
        this.recordAccessor = recordAccessor;
        this.completed = false;
        this.batched = false;

        // Get the descriptor at early to make sure mapping context is loaded and ready to handle inbound polymorphism.
        this.objectDescriptor = (recordClass != null) ? getMappingContext().getObjectDescriptor(recordClass) : null;
    }

    @Override
    public final boolean isCompleted() {
        return completed;
    }

    @Override
    public final boolean isBatched() {
        return batched;
    }

    @Override
    public final R get() throws ExecutionException {
        if (completed) {
            if (exception != null) {
                throw new ExecutionException(exception);
            } else {
                return result;
            }
        } else {
            throw new IllegalStateException("Operation not completed yet");
        }
    }

    @Override
    public void completed(R result, OperationStatistics statistics) {
        if (completed) {
            throw new IllegalStateException("Operation is already completed");
        }
        completed = true;
        this.result = result;
        this.statistics = statistics;

        if (getLogger().isInfoEnabled()) {
            MDC.put(STATISTICS_MDC_KEY, statistics.toString(ToStringStyle.SIMPLE_STYLE));
            getLogger().info((isBatched() ? "(Batched) " : "") + this);
            MDC.remove(STATISTICS_MDC_KEY);
        }
    }

    @Override
    public void failed(Throwable exception, OperationStatistics statistics) {
        if (completed) {
            throw new IllegalStateException("Operation is already completed");
        }
        completed = true;
        this.exception = exception;
        this.statistics = statistics;

        if (getLogger().isInfoEnabled()) {
            MDC.put(STATISTICS_MDC_KEY, statistics.toString(ToStringStyle.SIMPLE_STYLE));
            getLogger().info((isBatched() ? "(Batched) " : "") + this, exception);
            MDC.remove(STATISTICS_MDC_KEY);
        }
    }

    @Override
    public OperationStatistics getStatistics() {
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
}
