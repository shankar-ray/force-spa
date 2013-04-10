/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

/**
 * Thrown to indicate a specified object type was not found.
 */
public class ObjectNotFoundException extends RecordRequestException {
    private static final long serialVersionUID = 4772956989996123719L;

    /**
     * Constructs a new instance with <code>null</code> as the detail message.
     */
    public ObjectNotFoundException() {
        super();
    }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message
     */
    public ObjectNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified cause and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt>. This constructor is useful for exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public ObjectNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new instance with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
