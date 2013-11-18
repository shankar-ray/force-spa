/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.io.Serializable;
import java.net.URI;

/**
 * A connector which knows how to provide information about the currently authorized user, mostly for purposes of
 * configuring an outbound REST request.
 * <p/>
 * This abstraction gives the surrounding application flexibility in how it obtains and stores the information.
 */
public interface AuthorizationConnector extends Serializable {
    /**
     * Gets the HTTP "Authorization" header to use for outbound requests.
     *
     * @return the HTTP "Authorization" header
     */
    String getAuthorization();

    /**
     * Gets the instance URL to use for outbound requests.
     *
     * @return the instance URL
     */
    URI getInstanceUrl();

    /**
     * Gets the Salesforce user Id for which this authorization applies.
     *
     * @return the user Id
     */
    String getUserId();
}
