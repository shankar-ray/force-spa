/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.net.URI;

import com.force.spa.ApiVersion;

/**
 * A connector which knows how to issue requests to the Salesforce "data" REST API.
 * <p/>
 * This is a simple internal abstraction that allows different REST libraries to be plugged in. This means libraries
 * like Sun's Jersey or Apache HTTP.
 * <p/>
 * This interface is not intended to be some general purpose facade on top of HTTP libraries. It is, instead, very
 * specific to the task at hand.
 */
public interface RestConnector {

    /**
     * Issues a DELETE request to a Salesforce REST URI.
     *
     * @param uri             the relative URI. The protocol, host and path information should not be present and if
     *                        present they are ignored. Those pieces of information are supplied from the instance url
     *                        of the current authentication context. The path can be an absolute path starting with
     *                        "/services/data/vX.X" or the path can be a relative path (the portion after
     *                        "/services/data/vX.X"). If the path is relative then the "/services/data/vX.X" prefix is
     *                        automatically prepended.
     * @param responseHandler a handler that is invoked to help at key points during response processing. Ideally we'd
     *                        just have a completion handler but subtleties of the server batching response layout
     *                        require some additional handling before completion.
     */
    void delete(URI uri, RestResponseHandler<Void> responseHandler);

    /**
     * Issues a GET request to a Salesforce REST URI.
     *
     * @param uri             the relative URI. The protocol, host and path information should not be present and if
     *                        present they are ignored. Those pieces of information are supplied from the instance url
     *                        of the current authentication context. The path can be an absolute path starting with
     *                        "/services/data/vX.X" or the path can be a relative path (the portion after
     *                        "/services/data/vX.X"). If the path is relative then the "/services/data/vX.X" prefix is
     *                        automatically prepended.
     * @param responseHandler a handler that is invoked to help at key points during response processing. Ideally we'd
     *                        just have a completion handler but subtleties of the server batching response layout
     *                        require some additional handling before completion.
     */
    <T> void get(URI uri, RestResponseHandler<T> responseHandler);

    /**
     * Issues a PATCH request to a Salesforce REST URI.
     *
     * @param uri             the relative URI. The protocol, host and path information should not be present and if
     *                        present they are ignored. Those pieces of information are supplied from the instance url
     *                        of the current authentication context. The path can be an absolute path starting with
     *                        "/services/data/vX.X" or the path can be a relative path (the portion after
     *                        "/services/data/vX.X"). If the path is relative then the "/services/data/vX.X" prefix is
     *                        automatically prepended.
     * @param jsonBody        the JSON encoded body for the update request. See Salesforce REST documentation for more
     *                        details on the format.
     * @param responseHandler a handler that is invoked to help at key points during response processing. Ideally we'd
     *                        just have a completion handler but subtleties of the server batching response layout
     *                        require some additional handling before completion.
     */
    void patch(URI uri, String jsonBody, RestResponseHandler<Void> responseHandler);

    /**
     * Issues a POST request to a Salesforce REST URI.
     *
     * @param uri             the relative URI. The protocol, host and path information should not be present and if
     *                        present they are ignored. Those pieces of information are supplied from the instance url
     *                        of the current authentication context. The path can be an absolute path starting with
     *                        "/services/data/vX.X" or the path can be a relative path (the portion after
     *                        "/services/data/vX.X"). If the path is relative then the "/services/data/vX.X" prefix is
     *                        automatically prepended.
     * @param jsonBody        the JSON encoded body for the creation request. See Salesforce REST documentation for more
     *                        details on the format.
     * @param responseHandler a handler that is invoked to help at key points during response processing. Ideally we'd
     *                        just have a completion handler but subtleties of the server batching response layout
     *                        require some additional handling before completion.
     */
    <T> void post(URI uri, String jsonBody, RestResponseHandler<T> responseHandler);

    /**
     * Indicates whether this connector executes synchronously. Synchronous execution means that the request is
     * processed immediately and the callback is invoked before the original request returns.
     *
     * @return an indication of whether this is a synchronous connector
     */
    boolean isSynchronous();

    /**
     * Waits for all outstanding requests to complete. For synchronous connectors this method will return immediately.
     * For asynchronous or batched connectors this method may need to wait.
     */
    void join();

    /**
     * Returns the URI for the currently referenced Salesforce instance.
     * <p/>
     * The currently referenced Salesforce instance comes from the authorization connection and corresponds to the
     * organization context of the currently authorized user.
     *
     * @return the Salesforce API version
     */
    URI getInstanceUrl();

    /**
     * Returns the Salesforce API version to use for the currently referenced Salesforce instance.
     * <p/>
     * The currently referenced Salesforce instance comes from the authorization connection and corresponds to the
     * organization context of the currently authorized user.
     *
     * @return the Salesforce API version
     * @see #getInstanceUrl()
     */
    ApiVersion getApiVersion();
}
