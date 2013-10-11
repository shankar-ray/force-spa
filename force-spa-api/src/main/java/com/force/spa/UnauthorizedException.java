/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

/**
 * Thrown to indicate a problem with the authorization information for the request.
 */
public class UnauthorizedException extends RecordRequestException {

    private static final long serialVersionUID = 4324008953795920810L;

    /**
     * Constructs a new instance with <code>null</code> as the detail message.
     */
    public UnauthorizedException() {
        super();
    }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified cause and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt>. This constructor is useful for exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new instance with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
