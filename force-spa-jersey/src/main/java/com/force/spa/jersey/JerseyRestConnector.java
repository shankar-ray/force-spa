/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

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
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A {@link RestConnector} implementation that uses Sun's Jersey 1.x client to connect to Salesforce persistence using
 * the REST API.
 */
public final class JerseyRestConnector implements RestConnector {

    private static final Logger log = LoggerFactory.getLogger(JerseyRestConnector.class);
    private static final Cache<URI, ApiVersion> apiVersionCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    private final AuthorizationConnector authorizationConnector;
    private final Client client;
    private final ApiVersion apiVersion;
    private final ObjectReader objectReader;

    /**
     * Constructs a new instance.
     *
     * @param authorizationConnector the authorization connector used to obtain the instance URL
     * @param client                 the fully configured Jersey client
     * @param apiVersion             an optional apiVersion. If specified as <code>null</code>, then the highest version
     *                               supported by the server is used.
     */
    public JerseyRestConnector(AuthorizationConnector authorizationConnector, Client client, ApiVersion apiVersion) {
        this.authorizationConnector = authorizationConnector;
        this.client = client;
        this.apiVersion = apiVersion;
        this.objectReader = new ObjectMapper().reader();
    }

    @Override
    public void delete(URI uri, Map<String, String> headers, Callback<Void> callback) {
        try {
            try {
                ClientResponse response = getConfiguredResource(uri, headers).delete(ClientResponse.class);
                if (response.getStatus() >= 300) {
                    throw new UniformInterfaceException(response, true);
                }
                callback.onSuccess(null);
            } catch (UniformInterfaceException e) {
                String message = String.format("Delete failed: %s", buildErrorMessage(e));
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
    public void get(URI uri, Map<String, String> headers, Callback<JsonNode> callback) {
        try {
            try {
                InputStream resultStream = getConfiguredResource(uri, headers).get(InputStream.class);
                JsonNode resultNode = objectReader.readTree(resultStream);
                callback.onSuccess(resultNode);
            } catch (UniformInterfaceException e) {
                String message = String.format("Get failed: %s", buildErrorMessage(e));
                if (e.getResponse().getStatus() == 404) {
                    throw new RecordNotFoundException(message, e);
                } else {
                    throw new RecordRequestException(message, e);
                }
            } catch (IOException e) {
                throw new RecordResponseException("Failed to parse response stream", e);
            }
        } catch (RuntimeException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void patch(URI uri, String jsonBody, Map<String, String> headers, Callback<Void> callback) {
        try {
            try {
                ClientResponse response = getConfiguredResource(uri, headers).method("PATCH", ClientResponse.class, jsonBody);
                if (response.getStatus() >= 300) {
                    throw new UniformInterfaceException(response, true);
                }
                callback.onSuccess(null);
            } catch (UniformInterfaceException e) {
                String message = String.format("Patch failed: %s", buildErrorMessage(e));
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
    public void post(URI uri, String jsonBody, Map<String, String> headers, Callback<JsonNode> callback) {
        try {
            try {
                InputStream resultStream = getConfiguredResource(uri, headers).post(InputStream.class, jsonBody);
                JsonNode resultNode = objectReader.readTree(resultStream);
                callback.onSuccess(resultNode);
            } catch (UniformInterfaceException e) {
                String message = String.format("Post failed: %s", buildErrorMessage(e));
                if (e.getResponse().getStatus() == 404) {
                    throw new ObjectNotFoundException(message, e);
                } else {
                    throw new RecordRequestException(message, e);
                }
            } catch (IOException e) {
                throw new RecordResponseException("Failed to parse response stream", e);
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
        final URI instanceUri = authorizationConnector.getInstanceUrl();
        try {
            return apiVersionCache.get(instanceUri, new Callable<ApiVersion>() {
                @Override
                public ApiVersion call() throws JSONException {
                    return (apiVersion != null) ? apiVersion : getHighestSupportedApiVersion(instanceUri);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to determine version for instance %s", instanceUri), e);
        }
    }

    private ApiVersion getHighestSupportedApiVersion(URI instanceUri) throws JSONException {
        log.debug(String.format("Asking %s about Salesforce API versions", instanceUri));
        JSONArray versionChoices =
            client.resource(instanceUri).path("services/data")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(JSONArray.class);

        JSONObject highestVersion = (JSONObject) versionChoices.get(versionChoices.length() - 1);
        return new ApiVersion(highestVersion.get("version").toString());
    }

    private WebResource.Builder getConfiguredResource(URI relativeUri, Map<String, String> headers) {
        WebResource.Builder resourceBuilder =
            client.resource(buildAbsoluteUri(relativeUri))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                resourceBuilder = resourceBuilder.header(entry.getKey(), entry.getValue());
            }
        }
        return resourceBuilder;
    }

    private URI buildAbsoluteUri(URI relativeUri) {
        UriBuilder builder = UriBuilder.fromUri(relativeUri);
        if (!relativeUri.getPath().startsWith("/services/data")) {
            builder.replacePath("/services/data/v" + getApiVersion() + relativeUri.getPath());
        }
        builder.uri(authorizationConnector.getInstanceUrl());
        return builder.build();
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
