/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import java.net.URI;
import java.nio.channels.CompletionHandler;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.force.spa.ApiVersion;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordRequestException;
import com.force.spa.core.utils.CountingJsonParser;
import com.force.spa.core.MappingContext;
import com.force.spa.core.rest.RestConnector;
import com.force.spa.core.rest.RestVersionManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * A {@link RestConnector} implementation that uses Sun's Jersey 1.x client to connect to Salesforce persistence using
 * the REST API.
 */
final class JerseyRestConnector implements RestConnector {

    private final Client client;
    private final RecordAccessorConfig config;
    private final MappingContext mappingContext;
    private final RestVersionManager versionManager;

    JerseyRestConnector(RecordAccessorConfig config, MappingContext mappingContext, Client client) {
        this.config = config;
        this.mappingContext = mappingContext;
        this.client = client;

        this.versionManager = new RestVersionManager(this);
    }

    @Override
    public void delete(URI uri, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        try {
            try {
                ClientResponse response = getConfiguredResource(uri).delete(ClientResponse.class);
                try {
                    completionHandler.completed(parserFor(response), response.getStatus());
                } finally {
                    closeQuietly(response);
                }
            } catch (UniformInterfaceException e) {
                throw new RecordRequestException(e);
            }
        } catch (Exception e) {
            completionHandler.failed(e, null);
        }
    }

    @Override
    public void get(URI uri, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        try {
            try {
                ClientResponse response = getConfiguredResource(uri).get(ClientResponse.class);
                try {
                    completionHandler.completed(parserFor(response), response.getStatus());
                } finally {
                    closeQuietly(response);
                }

            } catch (UniformInterfaceException e) {
                throw new RecordRequestException(e);
            }
        } catch (Exception e) {
            completionHandler.failed(e, null);
        }
    }

    @Override
    public void patch(URI uri, String jsonBody, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        try {
            try {
                ClientResponse response = getConfiguredResource(uri).method("PATCH", ClientResponse.class, jsonBody);
                try {
                    completionHandler.completed(parserFor(response), response.getStatus());
                } finally {
                    closeQuietly(response);
                }

            } catch (UniformInterfaceException e) {
                throw new RecordRequestException(e);
            }
        } catch (Exception e) {
            completionHandler.failed(e, null);
        }
    }

    @Override
    public void post(URI uri, String jsonBody, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        try {
            try {
                ClientResponse response = getConfiguredResource(uri).post(ClientResponse.class, jsonBody);
                try {
                    completionHandler.completed(parserFor(response), response.getStatus());
                } finally {
                    closeQuietly(response);
                }

            } catch (UniformInterfaceException e) {
                throw new RecordRequestException(e);
            }
        } catch (Exception e) {
            completionHandler.failed(e, null);
        }
    }

    @Override
    public boolean isSynchronous() {
        return true;
    }

    @Override
    public void join() {
        // Nothing to flush for a synchronous connector.
    }

    @Override
    public URI getInstanceUrl() {
        return config.getAuthorizationConnector().getInstanceUrl();
    }

    @Override
    public ApiVersion getApiVersion() {
        if (config.getApiVersion() != null) {
            return config.getApiVersion();
        } else {
            return versionManager.getHighestSupportedVersion();
        }
    }

    /**
     * For unit test purposes only.
     */
    Client getClient() {
        return client;
    }

    private WebResource.Builder getConfiguredResource(URI relativeUri) {
        return client.resource(buildAbsoluteUri(relativeUri))
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE);
    }

    private URI buildAbsoluteUri(URI relativeUri) {
        UriBuilder builder = UriBuilder.fromUri(relativeUri);
        if (!relativeUri.getPath().startsWith("/services/data")) {
            builder.replacePath("/services/data/v" + getApiVersion() + relativeUri.getPath());
        }
        builder.uri(config.getAuthorizationConnector().getInstanceUrl());
        return builder.build();
    }

    private CountingJsonParser parserFor(ClientResponse response) {
        return mappingContext.createParser(response.getEntityInputStream());
    }

    private void closeQuietly(ClientResponse response) {
        try {
            response.close();
        } catch (ClientHandlerException e) {
            // Swallow the exception so a close failure doesn't obscure any exception already in the pipe
        }
    }
}
