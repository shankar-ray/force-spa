/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import static com.force.spa.jersey.ExtendedClientConfig.PROPERTY_CONNECTION_TIME_TO_LIVE;
import static com.force.spa.jersey.ExtendedClientConfig.PROPERTY_MAX_CONNECTIONS_PER_ROUTE;
import static com.force.spa.jersey.ExtendedClientConfig.PROPERTY_MAX_CONNECTIONS_TOTAL;
import static com.force.spa.jersey.ExtendedClientConfig.PROPERTY_SSL_SOCKET_FACTORY;
import static com.sun.jersey.api.client.config.ClientConfig.PROPERTY_CONNECT_TIMEOUT;
import static com.sun.jersey.api.client.config.ClientConfig.PROPERTY_READ_TIMEOUT;
import static com.sun.jersey.client.apache4.config.ApacheHttpClient4Config.PROPERTY_DISABLE_COOKIES;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.Validate;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;

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

    // Defaults for important basic Jersey properties
    static final long DEFAULT_CONNECT_TIMEOUT = MILLISECONDS.convert(5, SECONDS);
    static final long DEFAULT_READ_TIMEOUT = MILLISECONDS.convert(60, SECONDS);

    // Defaults for important Apache Jersey properties
    static final boolean DEFAULT_DISABLE_COOKIES = true;

    // Defaults for important Apache PooledClientConnectionManager properties
    static final long DEFAULT_CONNECTION_TIME_TO_LIVE = MILLISECONDS.convert(5, MINUTES);
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

        applyDefaultIfAbsent(clientConfig, PROPERTY_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        applyDefaultIfAbsent(clientConfig, PROPERTY_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
        applyDefaultIfAbsent(clientConfig, PROPERTY_DISABLE_COOKIES, DEFAULT_DISABLE_COOKIES);
        applyDefaultIfAbsent(clientConfig, PROPERTY_CONNECTION_TIME_TO_LIVE, DEFAULT_CONNECTION_TIME_TO_LIVE);
        applyDefaultIfAbsent(clientConfig, PROPERTY_MAX_CONNECTIONS_TOTAL, DEFAULT_MAX_CONNECTIONS_TOTAL);
        applyDefaultIfAbsent(clientConfig, PROPERTY_MAX_CONNECTIONS_PER_ROUTE, DEFAULT_MAX_CONNECTIONS_PER_ROUTE);

        if (hasNoConnectionManagerConfigured(clientConfig)) {
            configureConnectionManager(clientConfig);
        }

        return clientConfig;
    }

    private static <T> void applyDefaultIfAbsent(ClientConfig clientConfig, String propertyName, T defaultValue) {
        if (clientConfig.getProperty(propertyName) == null) {
            clientConfig.getProperties().put(propertyName, defaultValue);
        }
    }

    private static boolean hasNoConnectionManagerConfigured(ClientConfig clientConfig) {
        return clientConfig.getProperty(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER) == null;
    }

    private static void configureConnectionManager(ClientConfig clientConfig) {
        PoolingClientConnectionManager connectionManager =
            new PoolingClientConnectionManager(
                SchemeRegistryFactory.createDefault(), getConnectionTimeToLive(clientConfig), MILLISECONDS);
        connectionManager.setDefaultMaxPerRoute(getMaxConnectionsPerRoute(clientConfig));
        connectionManager.setMaxTotal(getMaxConnectionsTotal(clientConfig));

        if (hasSslSocketFactoryConfigured(clientConfig)) {
            connectionManager.getSchemeRegistry().register(new Scheme("https", 443, getSslSocketFactory(clientConfig)));
        }

        clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager);
    }

    private static long getConnectionTimeToLive(ClientConfig clientConfig) {
        return getProperty(clientConfig, PROPERTY_CONNECTION_TIME_TO_LIVE);
    }

    private static int getMaxConnectionsPerRoute(ClientConfig clientConfig) {
        return getProperty(clientConfig, PROPERTY_MAX_CONNECTIONS_PER_ROUTE);
    }

    private static int getMaxConnectionsTotal(ClientConfig clientConfig) {
        return getProperty(clientConfig, PROPERTY_MAX_CONNECTIONS_TOTAL);
    }

    private static boolean hasSslSocketFactoryConfigured(ClientConfig clientConfig) {
        return getSslSocketFactory(clientConfig) != null;
    }

    private static SSLSocketFactory getSslSocketFactory(ClientConfig clientConfig) {
        return getProperty(clientConfig, PROPERTY_SSL_SOCKET_FACTORY);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getProperty(ClientConfig clientConfig, String propertyName) {
        return (T) clientConfig.getProperty(propertyName);
    }

    private static void addAuthorizationFilter(ApacheHttpClient4 client, final AuthorizationConnector authorizationConnector) {
        client.addFilter(new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest clientRequest) {
                clientRequest.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, authorizationConnector.getAuthorization());
                return getNext().handle(clientRequest);
            }
        });
    }
}
