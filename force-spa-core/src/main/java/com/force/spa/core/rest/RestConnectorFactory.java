/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

/**
 * A factory that returns fully configured instances of {@link RestConnector}.
 */
public interface RestConnectorFactory {
    /**
     * Creates a new fully configured instance of {@link RestConnector} using values configured for the factory.
     *
     * @return the rest connector
     */
    RestConnector getRestConnector();
}
