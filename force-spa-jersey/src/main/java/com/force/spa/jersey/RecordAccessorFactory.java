/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.ApiVersion;
import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.rest.RestRecordAccessor;
import com.sun.jersey.api.client.Client;
import org.apache.commons.lang3.Validate;

/**
 * A simple (non-Spring) factory for instances of {@link RecordAccessor} that use a {@link JerseyRestConnector} for
 * communications.
 */
public class RecordAccessorFactory {

    private final ClientFactory clientFactory = new ClientFactory();
    private AuthorizationConnector defaultAuthorizationConnector = null; // Lazily populated.

    /**
     * Creates a new instance of {@link RecordAccessor} with a default {@link Client} and a default {@link
     * AuthorizationConnector} that uses an OAuth username-password flow with credential information retrieved from the
     * environment. The most current Salesforce API version is used by default.
     * <p/>
     * This form is likely not very useful in production environments because of the limited authorization support but
     * can be useful for integration tests.
     *
     * @param config configuration options
     * @return a RecordAccessor
     * @see PasswordAuthorizationConnector
     */
    public RecordAccessor newInstance(RecordAccessorConfig config) {
        return newInstance(config, getDefaultAuthorizationConnector());
    }

    /**
     * Creates a new instance of {@link RecordAccessor} with a default {@link Client} and a specific {@link
     * AuthorizationConnector}. The most current Salesforce API version is used by default.
     * <p/>
     * This is probably the most common constructor to use in production environments because it provides sufficient
     * control over authorization support but defaults everything else for simplicity.
     *
     * @param config                 configuration options
     * @param authorizationConnector an authorization connector
     * @return a RecordAccessor
     */
    public RecordAccessor newInstance(RecordAccessorConfig config, AuthorizationConnector authorizationConnector) {
        return newInstance(config, authorizationConnector, clientFactory.newInstance(authorizationConnector), null);
    }

    /**
     * Creates a new instance of {@link RecordAccessor} with all the parameters specified.
     * <p/>
     * You'll typically use this form if you want to supply a {@link Client} that is pre-configured with specific
     * filters or if you need full control over the api version.
     *
     * @param config                 configuration options
     * @param client                 a client instance
     * @param authorizationConnector an authorization connector
     * @param apiVersion             the desired Salesforce API version
     * @return a RecordAccessor
     */
    public RecordAccessor newInstance(RecordAccessorConfig config, AuthorizationConnector authorizationConnector, Client client, ApiVersion apiVersion) {
        Validate.notNull(config, "config must not be null");
        Validate.notNull(authorizationConnector, "authorizationConnector must not be null");
        Validate.notNull(client, "client must not be null");

        return new RestRecordAccessor(config, new JerseyRestConnector(authorizationConnector, client, apiVersion));
    }

    private AuthorizationConnector getDefaultAuthorizationConnector() {
        if (defaultAuthorizationConnector == null) {
            defaultAuthorizationConnector = new PasswordAuthorizationConnector();
        }
        return defaultAuthorizationConnector;
    }
}
