/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.force.spa.RecordAccessorConfig;

/**
 * A dummy implementation of {@link com.force.spa.MetadataAccessor} to help with unit tests.
 * <p/>
 * This initial implementation is not fully realized but as testing needs grow we can implement more and more.
 */
public class DummyMetadataAccessor extends AbstractMetadataAccessor {

    public DummyMetadataAccessor(RecordAccessorConfig config, MappingContext mappingContext) {
        super(config, mappingContext);
    }
}
