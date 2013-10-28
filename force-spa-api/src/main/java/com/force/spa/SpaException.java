/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

public class SpaException extends RuntimeException {

    private static final long serialVersionUID = 1903895475922758981L;

    /**
     * Convenience routine for dealing with {@link Exception} instances that may or may not be a SpaException.
     * <p/>
     * If the  exception is a SpaException then just return the given exception. Otherwise wrap the exception in a
     * SpaException.
     */
    public static SpaException valueOf(Throwable exception) {
        if (exception instanceof SpaException)
            return (SpaException) exception;
        else
            return new SpaException(exception);
    }

    /**
     * Constructs a new instance with <code>null</code> as the detail message.
     */
    public SpaException() {
        super();
    }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message
     */
    public SpaException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified cause and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt>. This constructor is useful for exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public SpaException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new instance with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public SpaException(String message, Throwable cause) {
        super(message, cause);
    }
}
