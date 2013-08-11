/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.force.spa.RestConnector;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * A {@link RestConnector} implementation that is designed for use in unit tests. The asynchronous callback interface of
 * the RestConnector is a pain to mock so this TestRestConnector wraps some synchronous methods that are much easier to
 * mock.
 */
public abstract class TestRestConnector implements RestConnector {

    private ObjectReader objectReader;

    public abstract void delete(URI uri);

    public abstract InputStream get(URI uri);

    public abstract void patch(URI uri, String jsonBody);

    public abstract InputStream post(URI uri, String jsonBody);

    @Override
    public final boolean isSynchronous() {
        return true;
    }

    @Override
    public final void flush() {
    }

    @Override
    public final void delete(URI uri, Callback<Void> callback) {
        try {
            delete(uri);
            callback.onSuccess(null);
        } catch (RuntimeException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public final void get(URI uri, Callback<JsonNode> callback) {
        try {
            InputStream resultStream = get(uri);
            JsonNode result = getObjectReader().readTree(resultStream);
            callback.onSuccess(result);
        } catch (RuntimeException e) {
            callback.onFailure(e);
        } catch (IOException e) {
            callback.onFailure(new RuntimeException(e));
        }
    }

    @Override
    public final void patch(URI uri, String jsonBody, Callback<Void> callback) {
        try {
            patch(uri, jsonBody);
            callback.onSuccess(null);
        } catch (RuntimeException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public final void post(URI uri, String jsonBody, Callback<JsonNode> callback) {
        try {
            InputStream resultStream = post(uri, jsonBody);
            JsonNode result = getObjectReader().readTree(resultStream);
            callback.onSuccess(result);
        } catch (RuntimeException e) {
            callback.onFailure(e);
        } catch (IOException e) {
            callback.onFailure(new RuntimeException(e));
        }
    }

    private ObjectReader getObjectReader() throws IOException {
        if (objectReader == null) {
            objectReader = new ObjectMapper().reader();
        }
        return objectReader;
    }
}
