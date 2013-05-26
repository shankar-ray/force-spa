/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.AuthorizationConnector;
import com.force.spa.ObjectNotFoundException;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordRequestException;
import com.force.spa.RestConnector;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
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
    private static final Cache<URI, String> versionedDataPathCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    private final AuthorizationConnector authorizationConnector;
    private final Client client;
    private final String apiVersion;

    /**
     * Constructs a new instance.
     *
     * @param authorizationConnector the authorization connector used to obtain the instance URL
     * @param client                 the fully configured Jersey client
     * @param apiVersion             an optional apiVersion. If specified as <code>null</code>, then the highest version
     *                               supported by the server is used.
     */
    public JerseyRestConnector(AuthorizationConnector authorizationConnector, Client client, String apiVersion) {
        this.authorizationConnector = authorizationConnector;
        this.client = client;
        this.apiVersion = apiVersion;
    }

    @Override
    public InputStream post(URI uri, String jsonBody, Map<String, String> headers) {
        try {
            return getConfiguredResource(uri, headers).post(InputStream.class, jsonBody);
        } catch (UniformInterfaceException e) {
            String message = String.format("Create failed: %s", extractMessage(e));
            if (e.getResponse().getStatus() == 404) {
                throw new ObjectNotFoundException(message, e);
            } else {
                throw new RecordRequestException(message, e);
            }
        }
    }

    @Override
    public InputStream get(URI uri, Map<String, String> headers) {
        try {
            return getConfiguredResource(uri, headers).get(InputStream.class);
        } catch (UniformInterfaceException e) {
            String message = String.format("Get failed: %s", extractMessage(e));
            if (e.getResponse().getStatus() == 404) {
                throw new RecordNotFoundException(message, e);
            } else {
                throw new RecordRequestException(message, e);
            }
        }
    }

    @Override
    public void patch(URI uri, String jsonBody, Map<String, String> headers) {
        try {
            ClientResponse response = getConfiguredResource(uri, headers).method("PATCH", ClientResponse.class, jsonBody);

            if (response.getStatus() >= 300) {
                throw new UniformInterfaceException(response, true);
            }
        } catch (UniformInterfaceException e) {
            String message = String.format("Update failed: %s", extractMessage(e));
            if (e.getResponse().getStatus() == 404) {
                throw new RecordNotFoundException(message, e);
            } else {
                throw new RecordRequestException(message, e);
            }
        }
    }

    @Override
    public void delete(URI uri, Map<String, String> headers) {
        try {
            ClientResponse response = getConfiguredResource(uri, headers).delete(ClientResponse.class);

            if (response.getStatus() >= 300) {
                throw new UniformInterfaceException(response, true);
            }
        } catch (UniformInterfaceException e) {
            String message = String.format("Delete failed: %s", extractMessage(e));
            if (e.getResponse().getStatus() == 404) {
                throw new RecordNotFoundException(message, e);
            } else {
                throw new RecordRequestException(message, e);
            }
        }
    }

    protected String getVersionedDataPath() {
        try {
            final URI instanceUri = authorizationConnector.getInstanceUrl();
            return versionedDataPathCache.get(instanceUri, new Callable<String>() {
                @Override
                public String call() throws JSONException {
                    if (apiVersion != null) {
                        return "/services/data/" + apiVersion;
                    } else {
                        return getDataPathForHighestSupportedVersion(instanceUri);
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine versioned data path for instance", e);
        }
    }

    private String getDataPathForHighestSupportedVersion(URI instanceUri) throws JSONException {
        log.debug(String.format("Asking %s about Salesforce API versions", instanceUri));
        JSONArray versionChoices =
            client.resource(instanceUri).path("services/data")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(JSONArray.class);

        JSONObject highestVersion = (JSONObject) versionChoices.get(versionChoices.length() - 1);
        return (String) highestVersion.get("url");
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
            builder.replacePath(getVersionedDataPath() + relativeUri.getPath());
        }
        builder.uri(authorizationConnector.getInstanceUrl());
        return builder.build();
    }

    private static String extractMessage(UniformInterfaceException e) {
        try {
            JSONArray jsonResponse = e.getResponse().getEntity(JSONArray.class);
            JSONObject errorObject = (JSONObject) jsonResponse.get(0);
            return errorObject.getString("message");
        } catch (Exception e1) {
            // Failed to extract Salesforce error message. There probably was none. Just return exception message.
            return e.getMessage();
        }
    }
}
