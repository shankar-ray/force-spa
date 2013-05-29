/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.util.Map;

/**
 * A connector which knows how to issue requests to the Salesforce "data" REST API.
 * <p/>
 * This is a simple internal abstraction that allows different REST libraries to be plugged in. This means libraries
 * like Sun's Jersey or Apache HTTP.
 * <p/>
 * This interface is not intended to be some general purpose facade on top of HTTP libraries. It is, instead, very
 * specific to the Salesforce REST task at hand.
 */
public interface RestConnector {
    /**
     * Issues a DELETE request to a Salesforce REST URI.
     *
     * @param uri      the relative URI. The protocol, host and path information should not be present and if present
     *                 they are ignored. Those pieces of information are supplied from the instance url of the current
     *                 authentication context. The path can be an absolute path starting with "/services/data/vX.X" or
     *                 the path can be a relative path (the portion after "/services/data/vX.X"). If the path is
     *                 relative then the "/services/data/vX.X" prefix is automatically prepended.
     * @param headers  optional HTTP headers to add to the request.
     * @param callback a callback that is invoked when the operation is complete
     */
    void delete(URI uri, Map<String, String> headers, Callback<Void> callback);

    /**
     * Issues a GET request to a Salesforce REST URI.
     *
     * @param uri      the relative URI. The protocol, host and path information should not be present and if present
     *                 they are ignored. Those pieces of information are supplied from the instance url of the current
     *                 authentication context. The path can be an absolute path starting with "/services/data/vX.X" or
     *                 the path can be a relative path (the portion after "/services/data/vX.X"). If the path is
     *                 relative then the "/services/data/vX.X" prefix is automatically prepended.
     * @param headers  optional HTTP headers to add to the request.
     * @param callback a callback that is invoked when the operation is complete
     */
    void get(URI uri, Map<String, String> headers, Callback<JsonNode> callback);

    /**
     * Issues a PATCH request to a Salesforce REST URI.
     *
     * @param uri      the relative URI. The protocol, host and path information should not be present and if present
     *                 they are ignored. Those pieces of information are supplied from the instance url of the current
     *                 authentication context. The path can be an absolute path starting with "/services/data/vX.X" or
     *                 the path can be a relative path (the portion after "/services/data/vX.X"). If the path is
     *                 relative then the "/services/data/vX.X" prefix is automatically prepended.
     * @param jsonBody the JSON encoded body for the update request. See Salesforce REST documentation for more details
     *                 on the format.
     * @param headers  optional HTTP headers to add to the request.
     * @param callback a callback that is invoked when the operation is complete
     */
    void patch(URI uri, String jsonBody, Map<String, String> headers, Callback<Void> callback);

    /**
     * Issues a POST request to a Salesforce REST URI.
     *
     * @param uri      the relative URI. The protocol, host and path information should not be present and if present
     *                 they are ignored. Those pieces of information are supplied from the instance url of the current
     *                 authentication context. The path can be an absolute path starting with "/services/data/vX.X" or
     *                 the path can be a relative path (the portion after "/services/data/vX.X"). If the path is
     *                 relative then the "/services/data/vX.X" prefix is automatically prepended.
     * @param jsonBody the JSON encoded body for the creation request. See Salesforce REST documentation for more
     *                 details on the format.
     * @param headers  optional HTTP headers to add to the request.
     * @param callback a callback that is invoked when the operation is complete
     */
    void post(URI uri, String jsonBody, Map<String, String> headers, Callback<JsonNode> callback);

    /**
     * Indicates whether this connector executes synchronously. Synchronous execution means that the request is
     * processed immediately and the callback is invoked before the original request returns.
     */
    boolean isSynchronous();

    /**
     * Completes any outstanding asynchronous requests that have been queued up and not yet executed.
     */
    void flush();

    /**
     * A Callback that is invoked with results at the end of a connector request.
     *
     * @param <T> The type of expected result
     */
    interface Callback<T> {
        void onSuccess(T result);

        void onFailure(RuntimeException exception);
    }
}
