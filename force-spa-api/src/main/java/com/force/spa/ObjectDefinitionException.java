/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

/**
 * Thrown to indicate a problem with how an annotated object is defined.
 */
public class ObjectDefinitionException extends SpaException {
    private static final long serialVersionUID = -4862812041864309165L;

    private final String name;

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param name    the name of the object
     * @param message the detail message
     */
    public ObjectDefinitionException(String name, String message) {
        super(String.format("%s, object=%s", message, name));
        this.name = name;
    }

    public final String getName() {
        return name;
    }
}
