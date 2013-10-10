/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import java.util.Map;

import org.apache.http.conn.ClientConnectionManager;
import org.springframework.stereotype.Component;

/**
 * A Spring bean for configuring the Jersey {@link com.sun.jersey.api.client.config.ClientConfig} used by {@link
 * SpringClientFactory}.
 *
 * @see SpringClientFactory
 */
@Component("spa.clientConfig")
public class SpringClientConfig extends SpaClientConfig {

    /**
     * Sets the connect timeout interval, in milliseconds.
     * <p/>
     * If not specified, the value defaults to 5000 (5 seconds).
     */
    public void setConnectionTimeout(long connectionTimeout) {
        getProperties().put(SpaClientConfig.PROPERTY_CONNECT_TIMEOUT, connectionTimeout);
    }

    /**
     * Sets the read timeout interval, in milliseconds.
     * <p/>
     * If not specified, the value defaults to 60000 (60 seconds).
     */
    public void setReadTimeout(long readTimeout) {
        getProperties().put(SpaClientConfig.PROPERTY_READ_TIMEOUT, readTimeout);
    }

    /**
     * Sets the maximum time to keep unused connections open in the Apache Http client pool, in milliseconds.
     * <p/>
     * If not specified, the value defaults to 300000 (5 minutes).
     */
    public void setConnectionTimeToLive(long connectionTimeToLive) {
        getProperties().put(SpaClientConfig.PROPERTY_CONNECTION_TIME_TO_LIVE, connectionTimeToLive);
    }

    /**
     * Sets the maximum total number of connections to maintain in the Apache Http client pool.
     * <p/>
     * This only applies to the case when no explicit {@link ClientConnectionManager} is specified. The value is used
     * when creating a default connection manager.
     * <p/>
     * If not specified, the value defaults to 100.
     */
    public void setMaxConnectionsTotal(int maxConnectionsTotal) {
        getProperties().put(SpaClientConfig.PROPERTY_MAX_CONNECTIONS_TOTAL, maxConnectionsTotal);
    }

    /**
     * Sets the maximum number of connections to maintain in the Apache Http client pool for a given route.
     * <p/>
     * This only applies to the case when no explicit {@link ClientConnectionManager} is specified. The value is used
     * when creating a default connection manager.
     * <p/>
     * If not specified, the value defaults to 20.
     */
    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        getProperties().put(SpaClientConfig.PROPERTY_MAX_CONNECTIONS_PER_ROUTE, maxConnectionsPerRoute);
    }

    /**
     * Sets the Apache Http {@link ClientConnectionManager} to use.
     * <p/>
     * If not specified, the value defaults to an instance of {@link org.apache.http.impl.conn.PoolingClientConnectionManager}.
     */
    public void setConnectionManager(ClientConnectionManager connectionManager) {
        getProperties().put(SpaClientConfig.PROPERTY_CONNECTION_MANAGER, connectionManager);
    }

    /**
     * Sets additional properties.
     */
    public void setProperties(Map<String, Object> properties) {
        getProperties().putAll(properties);
    }
}
