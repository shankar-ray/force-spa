/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.force.spa.AuthorizationConnector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.representation.Form;

/**
 * An implementation of {@link AuthorizationConnector} which uses a username and password to obtain the authorization
 * through an OAuth username-password flow performed at the time of construction.
 * <p/>
 * This class can also be used standalone (outside the context of SPA).
 */

//TODO Refactor to use connector? Better yet, refactor to use Soap so don't need to worry about OAuth config?
//TODO Seems like may be in wrong package, refactor to use any RestConnector? Add version that doesn't require OAuth configuration...

public class PasswordAuthorizationConnector implements AuthorizationConnector {

    private static final Logger log = LoggerFactory.getLogger(PasswordAuthorizationConnector.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String userId;
    private URI instanceUrl;
    private String authorization;

    /**
     * Constructs an instance all information necessary for the OAuth username-password flow coming from the
     * environment.
     * <p/>
     * The environment must contain: <ul> <li>FORCE_USERNAME - the username of a Salesforce user</li> <li>FORCE_PASSWORD
     * - the password of a Salesforce user</li> <li>FORCE_CLIENT_ID - the client id of a Salesforce connected
     * application</li> <li>FORCE_CLIENT_SECRET - the client secret of a Salesforce connected application</li> </ul>
     * <p/>
     * The environment can also contain: <ul> <li>FORCE_SERVER_URL - the Salesforce server URL</li> </ul>
     */
    public PasswordAuthorizationConnector() {
        this(
            getRequiredProperty("FORCE_USERNAME"),
            getRequiredProperty("FORCE_PASSWORD"));
    }

    /**
     * Constructs an instance with a username and password passed as parameters but the remaining information necessary
     * to perform an OAuth username-password flow passed coming from the environment.
     * <p/>
     * The environment must contain: <ul> <li>FORCE_CLIENT_ID - the client id of a Salesforce connected application</li>
     * <li>FORCE_CLIENT_SECRET - the client secret of a Salesforce connected application</li> </ul> The environment can
     * also contain: <ul> <li>FORCE_SERVER_URL - the Salesforce server URL</li> </ul>
     *
     * @param username the username of a Salesforce user
     * @param password the password of a Salesforce user
     */
    public PasswordAuthorizationConnector(String username, String password) {
        this(
            username,
            password,
            getRequiredProperty("FORCE_CLIENT_ID"),
            getRequiredProperty("FORCE_CLIENT_SECRET"),
            getDefaultedProperty("FORCE_SERVER_URL", "https://login.salesforce.com/"));
    }

    /**
     * Constructs an instance with all the information necessary to perform an OAuth username-password flow passed as
     * input parameters.
     *
     * @param username     the username of a Salesforce user
     * @param password     the password of a Salesforce user
     * @param clientId     the client id of a Salesforce connected application
     * @param clientSecret the client secret of a Salesforce connected application
     * @param serverUrl    the Salesforce server URL
     */
    public PasswordAuthorizationConnector(String username, String password, String clientId, String clientSecret, String serverUrl) {
        Validate.notEmpty(username, "username must be specified");
        Validate.notEmpty(password, "password must be specified");
        Validate.notEmpty(clientId, "clientId must be specified");
        Validate.notEmpty(clientSecret, "clientSecret must be specified");
        Validate.notEmpty(serverUrl, "serverUrl must be specified");

        InputStream jsonStream = null;
        Client client = Client.create();
        try {
            Form form = new Form();
            form.add("grant_type", "password");
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("username", username);
            form.add("password", password);

            jsonStream = client
                .resource(serverUrl)
                .path("services/oauth2/token")
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(InputStream.class, form);
            JsonNode jsonTree = objectMapper.readTree(jsonStream);

            userId = extractUserId(jsonTree.get("id").asText());
            instanceUrl = new URI(jsonTree.get("instance_url").asText());
            authorization = "OAuth " + jsonTree.get("access_token").asText();

        } catch (UniformInterfaceException e) {
            String message = String.format("Problem with OAuth token request: %s", extractSfdcErrorMessage(e));
            throw new RuntimeException(message, e);
        } catch (JsonProcessingException e) {
            String message = String.format("Problem with OAuth token response: %s", e.getMessage());
            log.error(message, e);
            throw new RuntimeException(message, e);
        } catch (IOException e) {
            String message = String.format("Problem reading OAuth token response stream: %s", e.getMessage());
            log.error(message, e);
            throw new RuntimeException(message, e);
        } catch (URISyntaxException e) {
            String message = String.format("Invalid OAuth server url: %s", e.getMessage());
            log.error(message, e);
            throw new RuntimeException(message, e);
        } finally {
            if (jsonStream != null) {
                try {
                    jsonStream.close();
                } catch (IOException e) {
                    // Ignore close failure. There is nothing we can do.
                }
            }
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

    public String getUserId() {
        return userId;
    }

    private static String getRequiredProperty(String name) {
        String value = getFromSystemOrEnvironment(name);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalStateException(String.format("Environment variable or System property %s is not set", name));
        }
        return value;
    }

    private static String getDefaultedProperty(String name, String defaultValue) {
        String value = getFromSystemOrEnvironment(name);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    private static String getFromSystemOrEnvironment(String name) {
        String value = System.getProperty(name);
        if (StringUtils.isEmpty(value)) {
            value = System.getenv(name);
        }
        return value;
    }

    private static String extractSfdcErrorMessage(UniformInterfaceException e) {
        try {
            InputStream jsonStream = e.getResponse().getEntity(InputStream.class);
            JsonNode jsonNode = objectMapper.readTree(jsonStream);

            return String.format("%s: %s", jsonNode.get("error").asText(), jsonNode.get("error_description").asText());
        } catch (JsonProcessingException e2) {
            return e.getMessage(); // Just use exception message
        } catch (IOException e2) {
            return e.getMessage(); // Just use exception message
        }
    }

    private static String extractUserId(String idUrlString) {
        Validate.notEmpty(idUrlString);
        return idUrlString.substring(idUrlString.lastIndexOf('/') + 1);
    }
}
