/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import org.apache.commons.lang3.Validate;

import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.MappingContext;
import com.force.spa.core.rest.RestConnector;
import com.force.spa.core.rest.RestRecordAccessor;
import com.sun.jersey.api.client.Client;

/**
 * A simple (non-Spring) factory for instances of {@link RecordAccessor} that use a {@link JerseyRestConnector} for
 * communications.
 */
public class RecordAccessorFactory {

    private final RecordAccessorConfig config;
    private final Client client;

    public RecordAccessorFactory(AuthorizationConnector authorizationConnector) {
        this(RecordAccessorConfig.DEFAULT.withAuthorizationConnector(authorizationConnector));
    }

    public RecordAccessorFactory(RecordAccessorConfig config) {
        this(config, createDefaultClient(config));
    }

    public RecordAccessorFactory(RecordAccessorConfig config, Client client) {
        Validate.notNull(config, "config must not be null");
        Validate.notNull(client, "client must not be null");

        this.config = config;
        this.client = client;
    }

    /**
     * Creates a new instance of {@link RecordAccessor} that uses REST and Jersey for network communications.
     *
     * @return the record accessor
     */
    public RecordAccessor getRecordAccessor() {
        MappingContext mappingContext = new MappingContext(config);
        RestConnector restConnector = new JerseyRestConnector(config, mappingContext, client);
        return new RestRecordAccessor(config, mappingContext, restConnector);
    }

    private static Client createDefaultClient(RecordAccessorConfig config) {
        return (config != null) ? new ClientFactory(config.getAuthorizationConnector()).getClient() : null;
    }
}
