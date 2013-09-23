/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.force.spa.RecordAccessorConfig;

/**
 * A cache of shared {@link ObjectMappingContext}s keyed by {@link com.force.spa.RecordAccessorConfig}. All @link
 * RecordAccessor}s that share the same configuration can share the same {@link ObjectMappingContext}. The contexts are
 * thread-safe and hold no per-accessor context. There is no reason to go through the expense of creating multiple
 * instances. This way we get to share the cache.
 */
public final class ObjectMappingContextCache {

    private final Map<RecordAccessorConfig, ObjectMappingContext> mappingContextsByConfig;

    public ObjectMappingContextCache() {
        mappingContextsByConfig = Collections.synchronizedMap(new HashMap<RecordAccessorConfig, ObjectMappingContext>());
    }

    public ObjectMappingContext getObjectMappingContext(RecordAccessorConfig config) {
        if (!mappingContextsByConfig.containsKey(config)) {
            mappingContextsByConfig.put(config, new ObjectMappingContext(config));
        }

        return mappingContextsByConfig.get(config);
    }
}
