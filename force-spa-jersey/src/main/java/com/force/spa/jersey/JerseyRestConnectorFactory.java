/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import org.apache.commons.lang3.Validate;

import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.MappingContext;
import com.force.spa.core.rest.RestConnector;
import com.force.spa.core.rest.RestConnectorFactory;
import com.sun.jersey.api.client.Client;

/**
 * A simple (non-Spring) factory for fully configured instances of {@link JerseyRestConnector}.
 */
public class JerseyRestConnectorFactory implements RestConnectorFactory {

    private final RecordAccessorConfig config;
    private final MappingContext mappingContext;
    private final Client client;

    public JerseyRestConnectorFactory(RecordAccessorConfig config, MappingContext mappingContext, Client client) {
        Validate.notNull(config, "config must not be null");
        Validate.notNull(client, "mappingContext must not be null");
        Validate.notNull(client, "client must not be null");

        this.config = config;
        this.mappingContext = mappingContext;
        this.client = client;
    }

    @Override
    public RestConnector getRestConnector() {
        return new JerseyRestConnector(config, mappingContext, client);
    }
}
