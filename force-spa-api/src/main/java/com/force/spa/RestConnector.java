/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.io.InputStream;
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
     * @return input stream for the response body returned by Salesforce.
     */
    InputStream post(URI uri, String jsonBody, Map<String, String> headers);

    /**
     * Issues a GET request to a Salesforce REST URI.
     *
     * @param uri     the relative URI. The protocol, host and path information should not be present and if present
     *                they are ignored. Those pieces of information are supplied from the instance url of the current
     *                authentication context. The path can be an absolute path starting with "/services/data/vX.X" or
     *                the path can be a relative path (the portion after "/services/data/vX.X"). If the path is relative
     *                then the "/services/data/vX.X" prefix is automatically prepended.
     * @param headers optional HTTP headers to add to the request.
     * @return input stream for the response body returned by Salesforce.
     */
    InputStream get(URI uri, Map<String, String> headers);

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
     */
    void patch(URI uri, String jsonBody, Map<String, String> headers);

    /**
     * Issues a DELETE request to a Salesforce REST URI.
     *
     * @param uri     the relative URI. The protocol, host and path information should not be present and if present
     *                they are ignored. Those pieces of information are supplied from the instance url of the current
     *                authentication context. The path can be an absolute path starting with "/services/data/vX.X" or
     *                the path can be a relative path (the portion after "/services/data/vX.X"). If the path is relative
     *                then the "/services/data/vX.X" prefix is automatically prepended.
     * @param headers optional HTTP headers to add to the request.
     */
    void delete(URI uri, Map<String, String> headers);
}
