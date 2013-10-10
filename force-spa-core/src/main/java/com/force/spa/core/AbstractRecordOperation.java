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

import com.force.spa.OperationStatistics;
import com.force.spa.RecordOperation;

/**
 * @param <T> the type of record the operation is working with
 * @param <R> the type of result expected from the operation
 */
public abstract class AbstractRecordOperation<T, R> implements RecordOperation<R>, CompletionHandler<R, OperationStatistics> {

    public static final String STATISTICS_MDC_KEY = "spa.statistics";

    private final ObjectDescriptor objectDescriptor;
    private final AbstractRecordAccessor recordAccessor;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private R result;
    private Throwable exception;
    private boolean completed = false;
    private String title;
    private Object detail;
    private OperationStatistics statistics;

    protected AbstractRecordOperation(AbstractRecordAccessor recordAccessor, Class<T> recordClass) {
        this.recordAccessor = recordAccessor;
        this.objectDescriptor = recordAccessor.getMappingContext().getObjectDescriptor(recordClass);
    }

    @Override
    public final boolean isCompleted() {
        return completed;
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

        if (log.isInfoEnabled()) {
            MDC.put(STATISTICS_MDC_KEY, statistics.toString(ToStringStyle.SIMPLE_STYLE));
            if (log.isDebugEnabled()) {
                log.debug(getTitle() + " completed: " + getDetail());
            } else {
                log.info(getTitle() + " completed");
            }
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

        if (log.isInfoEnabled()) {
            MDC.put(STATISTICS_MDC_KEY, statistics.toString(ToStringStyle.SIMPLE_STYLE));
            if (log.isDebugEnabled()) {
                log.debug(getTitle() + " failed: " + getDetail(), exception);
            } else {
                log.info(getTitle() + " failed", exception);
            }
            MDC.remove(STATISTICS_MDC_KEY);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Object getDetail() {
        return detail;
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

    public final void setTitle(String title) {
        this.title = title;
    }

    public final void setDetail(Object detail) {
        this.detail = detail;
    }

    protected final MappingContext getMappingContext() {
        return recordAccessor.getMappingContext();
    }
}
