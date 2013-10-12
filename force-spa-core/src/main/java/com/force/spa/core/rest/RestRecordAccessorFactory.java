/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import org.apache.commons.lang3.Validate;

import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorFactory;
import com.force.spa.core.MappingContext;

/**
 * A simple (non-Spring) factory for instances of {@link RecordAccessor}.
 */
public class RestRecordAccessorFactory implements RecordAccessorFactory {

    private final RecordAccessorConfig config;
    private final MappingContext mappingContext;
    private final RestConnector connector;

    public RestRecordAccessorFactory(RecordAccessorConfig config, MappingContext mappingContext, RestConnector connector) {
        Validate.notNull(config, "config must not be null");
        Validate.notNull(mappingContext, "mappingContext must not be null");
        Validate.notNull(connector, "connector must not be null");

        this.config = config;
        this.mappingContext = mappingContext;
        this.connector = connector;
    }

    @Override
    public RecordAccessor getRecordAccessor() {
        return new RestRecordAccessor(config, mappingContext, connector);
    }
}
