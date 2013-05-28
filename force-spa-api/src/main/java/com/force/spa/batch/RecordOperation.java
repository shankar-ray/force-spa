/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.batch;

public abstract class RecordOperation<T> {
    private T result;
    private RuntimeException exception;
    private State state = State.CREATED;

    public final boolean isStarted() {
        return state == State.STARTED;
    }

    public final boolean isCompleted() {
        return state == State.COMPLETED;
    }

    public final boolean isSuccessful() {
        return getException() == null;
    }

    public final RuntimeException getException() {
        if (isCompleted()) {
            return exception;
        } else {
            throw new IllegalStateException("Operation not completed yet");
        }
    }

    public final T getResult() {
        if (isSuccessful()) {
            return result;
        } else {
            throw new IllegalStateException("Operation failed, ask for the exception instead");
        }
    }

    protected final void setStarted() {
        if (isStarted()) {
            throw new IllegalStateException("Operation is already started");
        }

        state = State.STARTED;
    }

    public abstract void execute(RecordOperationVisitor executor);

    /**
     * Callback used by operation executors to signal successful completion of the operation. Applications should not
     * call this.
     *
     * @param result the result of the operation.
     */
    @SuppressWarnings("unchecked")
    public void onSuccess(Object result) {
        this.state = State.COMPLETED;
        this.result = (T) result;
        this.exception = null;
    }

    /**
     * Callback used by operation executors to signal successful completion of the operation. Applications should not
     * call this.
     */
    public void onSuccess() {
        onSuccess(null);
    }

    /**
     * Callback used by operation executors to signal failed completion of the operation. Applications should not call
     * this.
     *
     * @param exception the reason for the failure
     */
    public void onFailure(RuntimeException exception) {
        this.state = State.COMPLETED;
        this.result = null;
        this.exception = exception;
    }

    private static enum State {
        CREATED,
        STARTED,
        COMPLETED
    }
}
