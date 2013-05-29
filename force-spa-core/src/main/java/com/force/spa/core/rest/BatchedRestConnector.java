/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.force.spa.RestConnector;

import java.net.URI;
import java.util.Map;

// TODO Consider Batched connector will autoflush every 25?
public class BatchedRestConnector implements RestConnector {
    public BatchedRestConnector(RestConnector connector) {

    }

    @Override
    public boolean isSynchronous() {
        return false;
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(URI uri, Map<String, String> headers, Callback<Void> callback) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void get(URI uri, Map<String, String> headers, Callback<JsonNode> callback) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void patch(URI uri, String jsonBody, Map<String, String> headers, Callback<Void> callback) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void post(URI uri, String jsonBody, Map<String, String> headers, Callback<JsonNode> callback) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
