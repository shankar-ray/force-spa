/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.AbstractMetadataAccessor;
import com.force.spa.core.MappingContext;

/**
 * An implementation of {@link com.force.spa.RecordAccessor} that is based on the JSON representations of the Salesforce
 * REST API.
 */
public final class RestMetadataAccessor extends AbstractMetadataAccessor {

    private final RestConnector connector;

    public RestMetadataAccessor(RecordAccessorConfig config, MappingContext mappingContext, RestConnector connector) {
        super(config, mappingContext);
        this.connector = connector;
    }
}
