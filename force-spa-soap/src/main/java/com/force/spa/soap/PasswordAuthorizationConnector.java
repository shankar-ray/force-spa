/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.soap;

import java.net.URI;

import org.apache.commons.lang3.Validate;

import com.force.spa.AuthorizationConnector;
import com.force.spa.SpaException;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * An implementation of {@link com.force.spa.AuthorizationConnector} which uses a username and password to obtain the
 * authorization using the Salesforce partner API.
 * <p/>
 * Note that it may seem a bit odd to use a SOAP login when we mostly use REST API operations but the SOAP password
 * login easier to set up because it doesn't require connected app configuration.
 */
public class PasswordAuthorizationConnector implements AuthorizationConnector {

    private final URI instanceUrl;
    private final String authorization;

    public PasswordAuthorizationConnector(String serverUrl, String username, String password) {
        Validate.notEmpty(serverUrl, "serverUrl must be specified");
        Validate.notEmpty(username, "username must be specified");
        Validate.notEmpty(password, "password must be specified");

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(username);
        config.setPassword(password);
        config.setAuthEndpoint(serverUrl + "/services/Soap/u/28.0");

        try {
            new PartnerConnection(config); // Connects and stores results in partnerConfig as side effect

            authorization = "Bearer " + config.getSessionId();
            instanceUrl = URI.create(config.getServiceEndpoint());
        } catch (ConnectionException e) {
            throw new SpaException(e);
        }
    }

    @Override
    public final String getAuthorization() {
        return authorization;
    }

    @Override
    public final URI getInstanceUrl() {
        return instanceUrl;
    }
}
