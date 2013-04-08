/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.AuthorizationConnector;

import java.net.URI;

public class CustomAuthorizationConnector implements AuthorizationConnector {
    @Override
    public String getAuthorization() {
        return "OAuth 1999";
    }

    @Override
    public URI getInstanceUrl() {
        return URI.create("http://localhost:1999");
    }
}
