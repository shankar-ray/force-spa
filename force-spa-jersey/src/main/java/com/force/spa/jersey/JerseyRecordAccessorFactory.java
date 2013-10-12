/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.MappingContext;
import com.force.spa.core.MappingContextFactory;
import com.force.spa.core.rest.RestConnector;
import com.force.spa.core.rest.RestRecordAccessorFactory;
import com.sun.jersey.api.client.Client;

/**
 * A simple (non-Spring) factory for instances of {@link com.force.spa.RecordAccessor} based on a Jersey connector.
 * <p/>
 * This is really just a convenience wrapper for {@link RestRecordAccessorFactory} that supplies default values
 * appropriate for the Jersey implementation so that non-Spring creation can be simpler.
 */
public class JerseyRecordAccessorFactory extends RestRecordAccessorFactory {

    public JerseyRecordAccessorFactory(AuthorizationConnector authorizationConnector) {
        this(defaultRecordAccessorConfig(authorizationConnector));
    }

    public JerseyRecordAccessorFactory(RecordAccessorConfig config) {
        this(config, defaultMappingContext(config), defaultClient(config));
    }

    public JerseyRecordAccessorFactory(RecordAccessorConfig config, MappingContext mappingContext, Client client) {
        this(config, mappingContext, defaultConnector(config, mappingContext, client));
    }

    public JerseyRecordAccessorFactory(RecordAccessorConfig config, MappingContext mappingContext, RestConnector connector) {
        super(config, mappingContext, connector);
    }

    private static RecordAccessorConfig defaultRecordAccessorConfig(AuthorizationConnector authorizationConnector) {
        return RecordAccessorConfig.DEFAULT.withAuthorizationConnector(authorizationConnector);
    }

    private static MappingContext defaultMappingContext(RecordAccessorConfig config) {
        return new MappingContextFactory(config).getMappingContext();
    }

    private static Client defaultClient(RecordAccessorConfig config) {
        return new ClientFactory(config.getAuthorizationConnector()).getClient();
    }

    private static RestConnector defaultConnector(RecordAccessorConfig config, MappingContext mappingContext, Client client) {
        return new JerseyRestConnectorFactory(config, mappingContext, client).getRestConnector();
    }
}
