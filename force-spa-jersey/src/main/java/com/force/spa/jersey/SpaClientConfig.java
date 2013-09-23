/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

/**
 * A {@link com.sun.jersey.api.client.config.ClientConfig} for Jersey {@link com.sun.jersey.api.client.Client} instances
 * created by the {@link ClientFactory}.
 */
public class SpaClientConfig extends DefaultApacheHttpClient4Config {
    /**
     * The maximum total number of connections to maintain in the Apache Http client pool.
     * <p/>
     * The value MUST be an instance of {@link java.lang.Integer}.
     * <p/>
     * If not specified, the value defaults to 100.
     */
    public static final String PROPERTY_MAX_CONNECTIONS_TOTAL = "com.force.spa.jersey.apacheMaxConnectionsTotal";

    /**
     * The maximum number of connections to maintain in the Apache Http client pool for a given route.
     * <p/>
     * The value MUST be an instance of {@link java.lang.Integer}.
     * <p/>
     * If not specified, the value defaults to 20.
     */
    public static final String PROPERTY_MAX_CONNECTIONS_PER_ROUTE = "com.force.spa.jersey.apacheMaxConnectionsPerRoute";
}
