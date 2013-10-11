/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

/**
 * Thrown to indicate a query resulted in more rows than could be fetched using an asynchronous request.
 */
public class TooManyQueryRowsException extends RecordRequestException {

    private static final long serialVersionUID = 1772902936994694082L;

    /**
     * Constructs a new instance with a default detail message.
     */
    public TooManyQueryRowsException() {
        super("Too many result rows to be retrieved with an asynchronous request");
    }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message
     */
    public TooManyQueryRowsException(String message) {
        super(message);
    }
}
