/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import org.apache.commons.lang3.Validate;

import com.force.spa.RecordAccessorConfig;

/**
 * A simple (non-Spring) factory for fully configured instances of {@link MappingContext}.
 */
public final class MappingContextFactory {

    private final RecordAccessorConfig config;

    public MappingContextFactory(RecordAccessorConfig config) {
        Validate.notNull(config, "config must not be null");

        this.config = config;
    }

    public MappingContext getMappingContext() {
        return new MappingContext(config);
    }
}
