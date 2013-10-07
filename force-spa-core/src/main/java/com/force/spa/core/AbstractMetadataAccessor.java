/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.force.spa.MetadataAccessor;
import com.force.spa.RecordAccessorConfig;

public abstract class AbstractMetadataAccessor implements MetadataAccessor {

    private final RecordAccessorConfig config;
    private final MappingContext mappingContext;

    protected AbstractMetadataAccessor(RecordAccessorConfig config, MappingContext mappingContext) {
        this.config = config;
        this.mappingContext = mappingContext;
    }

    protected final RecordAccessorConfig getConfig() {
        return config;
    }

    protected final MappingContext getMappingContext() {
        return mappingContext;
    }
}
