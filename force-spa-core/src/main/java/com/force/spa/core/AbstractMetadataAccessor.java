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

    protected AbstractMetadataAccessor(RecordAccessorConfig config) {
        this.config = config;
    }

    protected final RecordAccessorConfig getConfig() {
        return config;
    }

    //TODO Not implemented yet
}
