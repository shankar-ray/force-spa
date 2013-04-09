/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

/**
 * Thrown to indicate a problem accessing records was returned in the response from the Salesforce server. The request
 * was successfully issued and the error is coming from the server.
 */
public class RecordResponseException extends RuntimeException {
    private static final long serialVersionUID = 1028782519413158664L;

    /**
     * Constructs a new instance with <code>null</code> as the detail message.
     */
    public RecordResponseException() {
        super();
    }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message
     */
    public RecordResponseException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified cause and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt>. This constructor is useful for exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public RecordResponseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new instance with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public RecordResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
