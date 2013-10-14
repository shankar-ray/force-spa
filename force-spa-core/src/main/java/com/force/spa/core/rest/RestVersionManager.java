/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import static com.force.spa.SpaException.getCauseAsSpaException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.force.spa.ApiVersion;
import com.force.spa.Statistics;
import com.force.spa.core.utils.CountingJsonParser;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * A cache of information about the highest supported API version of instances.
 */
public final class RestVersionManager {

    private static final Logger LOG = LoggerFactory.getLogger(BatchRestConnector.class);

    static final ApiVersion DEFAULT_API_VERSION = new ApiVersion("28.0");

    private final RestConnector connector;
    private final Cache<URI, ApiVersion> highestSupportedVersionCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    public RestVersionManager(RestConnector connector) {
        this.connector = connector;
    }

    public ApiVersion getHighestSupportedVersion() {
        try {
            return highestSupportedVersionCache.get(connector.getInstanceUrl(), new Callable<ApiVersion>() {
                @Override
                public ApiVersion call() {
                    List<ApiVersion> supportedVersions = getSupportedVersions();
                    return supportedVersions.get(supportedVersions.size() - 1);
                }
            });
        } catch (ExecutionException | UncheckedExecutionException e) {
            throw getCauseAsSpaException(e);
        }
    }

    private List<ApiVersion> getSupportedVersions() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting supported API versions for " + connector.getInstanceUrl());
        }

        final List<ApiVersion> supportedVersions = new ArrayList<>();
        connector.get(URI.create("/services/data"), new RestResponseHandler<VersionInfo[]>() {
            @Override
            public VersionInfo[] deserialize(CountingJsonParser parser) throws IOException {
                return parser.readValueAs(VersionInfo[].class);
            }

            @Override
            public void completed(VersionInfo[] versionInfos, Statistics statistics) {
                if (versionInfos != null && versionInfos.length > 0) {
                    for (VersionInfo versionInfo : versionInfos) {
                        supportedVersions.add(new ApiVersion(versionInfo.getVersion()));
                    }
                } else {
                    supportedVersions.add(DEFAULT_API_VERSION);
                }
            }

            @Override
            public void failed(Throwable exception, Statistics statistics) {
                LOG.error("Failed to determine supported API versions for " + connector.getInstanceUrl());
                supportedVersions.add(DEFAULT_API_VERSION);
            }
        });
        connector.join();

        return supportedVersions;
    }
}
