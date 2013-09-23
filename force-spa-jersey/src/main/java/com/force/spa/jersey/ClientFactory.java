/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.Validate;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import com.force.spa.AuthorizationConnector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

/**
 * A simple (non-Spring) factory for instances of {@link Client} configured appropriately for {@link
 * JerseyRestConnector} use.
 * <p/>
 * This factory creates instances of {@link ApacheHttpClient4} because the Apache client is needed to support the HTTP
 * "PATCH" request required by the Salesforce API. If you decide not to use this factory and instead provide a client
 * instance of your own then make sure the client instance can support HTTP "PATCH". You really should use this factory
 * or something derived from it.
 * <p/>
 * By default, the returned instances use a {@link PoolingClientConnectionManager} in order to support multi-threaded
 * use.
 */
public final class ClientFactory {

    static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 20;
    static final int DEFAULT_MAX_CONNECTIONS_TOTAL = 100;

    private final AuthorizationConnector authorizationConnector;
    private final ClientConfig clientConfig;

    public ClientFactory(AuthorizationConnector authorizationConnector) {
        this(authorizationConnector, new DefaultApacheHttpClient4Config());
    }

    public ClientFactory(AuthorizationConnector authorizationConnector, ClientConfig clientConfig) {
        Validate.notNull(authorizationConnector, "authorizationConnector must not be null");
        Validate.notNull(clientConfig, "clientConfig must not be null");

        this.authorizationConnector = authorizationConnector;
        this.clientConfig = augmentClientConfig(clientConfig);
    }

    /**
     * Creates a new instance of {@link Client} configured appropriately for {@link JerseyRestConnector} use.
     *
     * @return a Jersey Client
     */
    public Client getClient() {
        ApacheHttpClient4 client = ApacheHttpClient4.create(clientConfig);

        addAuthorizationFilter(client, authorizationConnector);

        return client;
    }

    private static ClientConfig augmentClientConfig(ClientConfig clientConfig) {
        if (hasNoConnectionManagerConfigured(clientConfig)) {
            configureConnectionManager(clientConfig);
        }
        return clientConfig;
    }

    private static boolean hasNoConnectionManagerConfigured(ClientConfig clientConfig) {
        return clientConfig.getProperty(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER) == null;
    }

    private static void configureConnectionManager(ClientConfig clientConfig) {
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(getMaxConnectionsPerRoute(clientConfig));
        connectionManager.setMaxTotal(getMaxConnectionsTotal(clientConfig));

        clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager);
    }

    private static int getMaxConnectionsPerRoute(ClientConfig clientConfig) {
        return getProperty(clientConfig, SpaClientConfig.PROPERTY_MAX_CONNECTIONS_PER_ROUTE, DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
    }

    private static int getMaxConnectionsTotal(ClientConfig clientConfig) {
        return getProperty(clientConfig, SpaClientConfig.PROPERTY_MAX_CONNECTIONS_TOTAL, DEFAULT_MAX_CONNECTIONS_TOTAL);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getProperty(ClientConfig clientConfig, String propertyName, T defaultValue) {
        T configuredValue = (T) clientConfig.getProperty(propertyName);
        return configuredValue != null ? configuredValue : defaultValue;
    }

    private static void addAuthorizationFilter(ApacheHttpClient4 client, final AuthorizationConnector authorizationConnector) {
        client.addFilter(new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest clientRequest) {
                clientRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, authorizationConnector.getAuthorization());
                return getNext().handle(clientRequest);
            }
        });
    }
}
