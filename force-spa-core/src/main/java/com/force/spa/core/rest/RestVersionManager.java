/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import static com.force.spa.SpaException.getCauseAsSpaException;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonParseException;
import com.force.spa.ApiVersion;
import com.force.spa.SpaException;
import com.force.spa.core.CountingJsonParser;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * A cache of information about the highest supported API version of instances.
 */
public final class RestVersionManager {

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
        } catch (ExecutionException e) {
            throw getCauseAsSpaException(e);
        } catch (UncheckedExecutionException e) {
            throw getCauseAsSpaException(e);
        }
    }

    private List<ApiVersion> getSupportedVersions() {
        final List<ApiVersion> supportedVersions = new ArrayList<ApiVersion>();

        connector.get(URI.create("/services/data"), new CompletionHandler<CountingJsonParser, Integer>() {
            @Override
            public void completed(CountingJsonParser parser, Integer status) {
                try {
                    VersionInfo[] versionInfos = parser.readValueAs(VersionInfo[].class);
                    if (versionInfos != null && versionInfos.length > 0) {
                        for (VersionInfo versionInfo : versionInfos) {
                            supportedVersions.add(new ApiVersion(versionInfo.getVersion()));
                        }
                    } else {
                        throw new JsonParseException("Failed to parse any version information in the response", parser.getCurrentLocation());
                    }
                } catch (IOException e) {
                    throw new SpaException("Failed to parse version response from " + connector.getInstanceUrl(), e);
                }
            }

            @Override
            public void failed(Throwable exception, Integer status) {
                throw new SpaException("Failed to determine supported Api Versions for " + connector.getInstanceUrl(), exception);
            }
        });
        connector.join();

        return supportedVersions;
    }

    @SuppressWarnings("UnusedDeclaration")
    private static class VersionInfo {
        private String version;
//        private String label;
//        private String url;

        private String getVersion() {
            return version;
        }

        private void setVersion(String version) {
            this.version = version;
        }
    }
}
