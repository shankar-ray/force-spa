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
public class ExtendedClientConfig extends DefaultApacheHttpClient4Config {
    /**
     * The maximum time to keep unused connections open in the Apache Http client pool, in milliseconds.
     * <p/>
     * The value MUST be an instance of {@link java.lang.Integer} or {@link java.lang.Long}.
     * <p/>
     * If not specified, the value defaults to 300000 (5 minutes).
     */
    public static final String PROPERTY_CONNECTION_TIME_TO_LIVE = "com.force.spa.jersey.apacheConnectionTimeToLive";

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
     *
     * @see
     */
    public static final String PROPERTY_MAX_CONNECTIONS_PER_ROUTE = "com.force.spa.jersey.apacheMaxConnectionsPerRoute";

    /**
     * A specific SSL socket factory implementation to use.
     * <p/>
     * The value MUST be an instance of {@link org.apache.http.conn.ssl.SSLSocketFactory}.
     * <p/>
     * If not specified, Apache defaults are used.
     */
    public static final String PROPERTY_SSL_SOCKET_FACTORY = "com.force.spa.jersey.apacheSslSocketFactory";
}
