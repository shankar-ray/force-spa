/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.force.spa.ApiVersion;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordRequestException;
import com.force.spa.RecordResponseException;
import com.force.spa.Statistics;
import com.force.spa.core.MappingContext;
import com.force.spa.core.rest.RestConnector;
import com.force.spa.core.rest.RestResponseHandler;
import com.force.spa.core.rest.RestVersionManager;
import com.force.spa.core.utils.CountingJsonParser;
import com.google.common.base.Stopwatch;
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

    private static final Logger LOG = LoggerFactory.getLogger(JerseyRestConnector.class);

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
    public void delete(URI uri, RestResponseHandler<Void> responseHandler) {
        CountingJsonParser parser = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            ClientResponse response = getConfiguredResource(uri).delete(ClientResponse.class);
            try {
                parser = parserFor(response);
                responseHandler.handleStatus(response.getStatus(), parser);
                responseHandler.completed(null, buildStatistics(stopwatch, null, parser));
            } finally {
                closeQuietly(response);
            }
        } catch (Exception e) {
            responseHandler.failed(mapSelectedExceptions(e), buildStatistics(stopwatch, null, parser));
        }
    }

    @Override
    public <T> void get(URI uri, RestResponseHandler<T> responseHandler) {
        CountingJsonParser parser = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            ClientResponse response = getConfiguredResource(uri).get(ClientResponse.class);
            try {
                parser = parserFor(response);
                responseHandler.handleStatus(response.getStatus(), parser);
                T result = (response.getStatus() < 300) ? responseHandler.deserialize(parser) : null;
                responseHandler.completed(result, buildStatistics(stopwatch, null, parser));
            } finally {
                closeQuietly(response);
            }
        } catch (Exception e) {
            responseHandler.failed(mapSelectedExceptions(e), buildStatistics(stopwatch, null, parser));
        }
    }

    @Override
    public void patch(URI uri, String jsonBody, RestResponseHandler<Void> responseHandler) {

        if (LOG.isTraceEnabled()) {
            LOG.trace("Request body: " + jsonBody);
        }

        CountingJsonParser parser = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            ClientResponse response = getConfiguredResource(uri).method("PATCH", ClientResponse.class, jsonBody);
            try {
                parser = parserFor(response);
                responseHandler.handleStatus(response.getStatus(), parser);
                responseHandler.completed(null, buildStatistics(stopwatch, jsonBody, parser));
            } finally {
                closeQuietly(response);
            }
        } catch (Exception e) {
            responseHandler.failed(mapSelectedExceptions(e), buildStatistics(stopwatch, jsonBody, parser));
        }
    }

    @Override
    public <T> void post(URI uri, String jsonBody, RestResponseHandler<T> responseHandler) {

        if (LOG.isTraceEnabled()) {
            LOG.trace("Request body: " + jsonBody);
        }

        CountingJsonParser parser = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            ClientResponse response = getConfiguredResource(uri).post(ClientResponse.class, jsonBody);
            try {
                parser = parserFor(response);
                responseHandler.handleStatus(response.getStatus(), parser);
                T result = (response.getStatus() < 300) ? responseHandler.deserialize(parser) : null;
                responseHandler.completed(result, buildStatistics(stopwatch, jsonBody, parser));
            } finally {
                closeQuietly(response);
            }
        } catch (Exception e) {
            responseHandler.failed(mapSelectedExceptions(e), buildStatistics(stopwatch, null, parser));
        }
    }

    @Override
    public boolean isSynchronous() {
        return true;
    }

    @Override
    public void join() {
        // Nothing to do for a synchronous connector.
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

    private WebResource.Builder getConfiguredResource(URI relativeUri) {
        return client.resource(buildAbsoluteUri(relativeUri))
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .header("X-Chatter-Entity-Encoding", false);
    }

    private URI buildAbsoluteUri(URI relativeUri) {
        UriBuilder builder = UriBuilder.fromUri(relativeUri);
        if (!relativeUri.getPath().startsWith("/services/data")) {
            builder.replacePath("/services/data/v" + getApiVersion() + relativeUri.getPath());
        }
        builder.uri(config.getAuthorizationConnector().getInstanceUrl());
        return builder.build();
    }

    private CountingJsonParser parserFor(ClientResponse response) throws IOException {
        InputStream responseStream = response.getEntityInputStream();
        if (LOG.isTraceEnabled()) {
            ByteArrayOutputStream bufferedResponseStream = new ByteArrayOutputStream(8192);
            IOUtils.copy(responseStream, bufferedResponseStream);
            byte[] streamBytes = bufferedResponseStream.toByteArray();
            responseStream = new ByteArrayInputStream(streamBytes);

            LOG.trace("Response body: " + new String(streamBytes, Charset.forName("UTF-8")));
        }
        return mappingContext.createParser(responseStream);
    }

    private void closeQuietly(ClientResponse response) {
        try {
            response.close();
        } catch (ClientHandlerException e) {
            // Swallow the exception so a close failure doesn't obscure any exception already in the pipe
        }
    }

    private Statistics buildStatistics(Stopwatch stopwatch, String jsonBody, CountingJsonParser parser) {
        return new Statistics.Builder()
            .bytesSent((jsonBody != null) ? jsonBody.length() : 0)
            .bytesReceived((parser != null) ? parser.getCount() : 0)
            .elapsedNanos(stopwatch.elapsed(TimeUnit.NANOSECONDS))
            .build();
    }

    private static Exception mapSelectedExceptions(Exception exception) {
        if (exception instanceof JsonParseException) {
            return new RecordResponseException("Failed to parse response body", exception);
        } else if (exception instanceof UniformInterfaceException) {
            return new RecordRequestException(exception.getMessage(), exception);
        } else {
            return exception;
        }
    }
}
