/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.io.Serializable;
import java.net.URI;

/**
 * A connector which knows how to access the results of a Salesforce OAuth exchange for the purpose of configuring an
 * outbound REST request.
 * <p/>
 * This abstraction gives the surrounding application flexibility in how it obtains and stores the information.
 */
public interface AuthorizationConnector extends Serializable {
    /**
     * Gets the value of the authorization header to use for an outbound REST request.
     *
     * @return a value for the Authorization header
     */
    String getAuthorization();

    /**
     * Gets the instance URL to use for an outbound REST request.
     *
     * @return the instance URL
     */
    URI getInstanceUrl();
}
