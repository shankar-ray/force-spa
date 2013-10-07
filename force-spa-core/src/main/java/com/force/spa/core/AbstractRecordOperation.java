/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.force.spa.OperationStatistics;
import com.force.spa.RecordOperation;
import com.force.spa.core.utils.MDCUtils;

/**
 * @param <T> the type of record the operation is working with
 * @param <R> the type of result expected from the operation
 */
public abstract class AbstractRecordOperation<T, R> implements RecordOperation<R>, CompletionHandler<R, OperationStatistics> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRecordOperation.class);

    private static final String BYTES_SENT_MDC_KEY = "spa.bytesSent";
    private static final String BYTES_RECEIVED_MDC_KEY = "spa.bytesReceived";
    private static final String ELAPSED_MICROS_MDC_KEY = "spa.elapsedMicros";

    private final ObjectDescriptor objectDescriptor;
    private final AbstractRecordAccessor recordAccessor;

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

        MDCUtils.add(BYTES_RECEIVED_MDC_KEY, statistics.getBytesReceived());
        MDCUtils.add(BYTES_SENT_MDC_KEY, statistics.getBytesSent());
        MDCUtils.add(ELAPSED_MICROS_MDC_KEY, statistics.getElapsedMicros());

        if (LOG.isInfoEnabled()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(getTitle() + ", statistics=" + statistics + ", detail=" + getDetail());
            } else {
                LOG.info(getTitle() + ", statistics=" + statistics);
            }
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

        MDCUtils.add(BYTES_RECEIVED_MDC_KEY, statistics.getBytesReceived());
        MDCUtils.add(BYTES_SENT_MDC_KEY, statistics.getBytesSent());
        MDCUtils.add(ELAPSED_MICROS_MDC_KEY, statistics.getElapsedMicros());

        if (LOG.isInfoEnabled()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(getTitle() + " failed, statistics=" + statistics + ", detail=" + getDetail(), exception);
            } else {
                LOG.info(getTitle() + " failed, statistics=" + statistics, exception);
            }
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
