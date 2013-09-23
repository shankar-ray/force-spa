/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.force.spa.ApiVersion;
import com.force.spa.AuthorizationConnector;
import com.force.spa.ObjectNotFoundException;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordRequestException;
import com.force.spa.RecordResponseException;
import com.force.spa.RestConnector;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * A {@link RestConnector} implementation that uses Sun's Jersey 1.x client to connect to Salesforce persistence using
 * the REST API.
 */
final class JerseyRestConnector implements RestConnector {

    private static final Logger LOG = LoggerFactory.getLogger(JerseyRestConnector.class);

    private static final ObjectReader objectReader = new ObjectMapper().reader();
    private static final Cache<URI, ApiVersion> apiVersionCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    private final Client client;
    private final ApiVersion configuredApiVersion;
    private final AuthorizationConnector authorizationConnector;

    JerseyRestConnector(Client client, AuthorizationConnector authorizationConnector, ApiVersion apiVersion) {
        this.client = client;
        this.authorizationConnector = authorizationConnector;
        this.configuredApiVersion = apiVersion;
    }

    @Override
    public void delete(URI uri, Callback<Void> callback) {
        try {
            try {
                ClientResponse response = getConfiguredResource(uri).delete(ClientResponse.class);
                if (response.getStatus() >= 300) {
                    throw new UniformInterfaceException(response, true);
                }
                callback.onSuccess(null);
            } catch (UniformInterfaceException e) {
                String message = "Delete failed: " + buildErrorMessage(e);
                if (e.getResponse().getStatus() == 404) {
                    throw new RecordNotFoundException(message, e);
                } else {
                    throw new RecordRequestException(message, e);
                }
            }
        } catch (RuntimeException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void get(URI uri, Callback<JsonNode> callback) {
        try {
            try {
                InputStream resultStream = getConfiguredResource(uri).get(InputStream.class);
                JsonNode resultNode = readTree(resultStream);
                callback.onSuccess(resultNode);
            } catch (UniformInterfaceException e) {
                String message = "Get failed: " + buildErrorMessage(e);
                if (e.getResponse().getStatus() == 404) {
                    throw new RecordNotFoundException(message, e);
                } else {
                    throw new RecordRequestException(message, e);
                }
            }
        } catch (RuntimeException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void patch(URI uri, String jsonBody, Callback<Void> callback) {
        try {
            try {
                ClientResponse response = getConfiguredResource(uri).method("PATCH", ClientResponse.class, jsonBody);
                if (response.getStatus() >= 300) {
                    throw new UniformInterfaceException(response, true);
                }
                callback.onSuccess(null);
            } catch (UniformInterfaceException e) {
                String message = "Patch failed: " + buildErrorMessage(e);
                if (e.getResponse().getStatus() == 404) {
                    throw new RecordNotFoundException(message, e);
                } else {
                    throw new RecordRequestException(message, e);
                }
            }
        } catch (RuntimeException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void post(URI uri, String jsonBody, Callback<JsonNode> callback) {
        try {
            try {
                InputStream resultStream = getConfiguredResource(uri).post(InputStream.class, jsonBody);
                JsonNode resultNode = readTree(resultStream);
                callback.onSuccess(resultNode);
            } catch (UniformInterfaceException e) {
                String message = "Post failed: " + buildErrorMessage(e);
                if (e.getResponse().getStatus() == 404) {
                    throw new ObjectNotFoundException(message, e);
                } else {
                    throw new RecordRequestException(message, e);
                }
            }
        } catch (RuntimeException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public boolean isSynchronous() {
        return true;
    }

    @Override
    public void flush() {
        // Nothing to flush for a synchronous connector.
    }

    @Override
    public ApiVersion getApiVersion() {
        if (configuredApiVersion == null) {
            final URI instanceUri = authorizationConnector.getInstanceUrl();
            try {
                return apiVersionCache.get(instanceUri, new Callable<ApiVersion>() {
                    @Override
                    public ApiVersion call() throws JSONException {
                        LOG.debug(String.format("Asking %s about Salesforce API versions", instanceUri));
                        JSONArray versionChoices =
                            client.resource(instanceUri).path("services/data")
                                .accept(MediaType.APPLICATION_JSON_TYPE)
                                .get(JSONArray.class);

                        JSONObject highestVersion = (JSONObject) versionChoices.get(versionChoices.length() - 1);
                        return new ApiVersion(highestVersion.get("version").toString());
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(String.format("Failed to determine version for instance %s", instanceUri), e);
            }
        } else {
            return configuredApiVersion;
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
        builder.uri(authorizationConnector.getInstanceUrl());
        return builder.build();
    }

    private static JsonNode readTree(InputStream inputStream) {
        try {
            return objectReader.readTree(inputStream);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to parse response stream", e);
        }
    }

    private static String buildErrorMessage(UniformInterfaceException e) {
        try {
            StringBuilder builder = new StringBuilder(80);
            JSONArray jsonResponse = e.getResponse().getEntity(JSONArray.class);
            for (int i = 0, limit = jsonResponse.length(); i < limit; i++) {
                JSONObject error = (JSONObject) jsonResponse.get(i);
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                String errorCode = StringUtils.defaultIfEmpty(error.has("errorCode") ? error.getString("errorCode") : null, null);
                if (errorCode != null) {
                    builder.append(errorCode);
                }
                String message = StringUtils.defaultIfEmpty(error.has("message") ? error.getString("message") : null, null);
                if (message != null) {
                    if (builder.length() > 0)
                        builder.append(": ");
                    builder.append(message);
                }
                String fields = StringUtils.defaultIfEmpty(error.has("fields") ? error.get("fields").toString() : null, null);
                if (fields != null && !(fields.equals("[]"))) {
                    if (builder.length() > 0)
                        builder.append(": ");
                    builder.append(fields);
                }
            }
            if (builder.length() > 0) {
                return builder.toString();
            } else {
                return e.toString();
            }
        } catch (Exception e1) {
            return e.toString(); // Failed to extract a Salesforce message. Just return exception message.
        }
    }
}
