/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;

import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordNotFoundException;
import com.force.spa.jersey.JerseyRecordAccessorFactory;
import com.force.spa.soap.PasswordAuthorizationConnector;

public class AbstractRecordAccessorIntegrationTest {

    private RecordAccessor recordAccessor;

    protected final Set<Object> objects = new HashSet<>();

    @Before
    public void setUpRecordAccessor() {
        String username = getRequiredProperty("FORCE_USERNAME");
        String password = getRequiredProperty("FORCE_PASSWORD");
        String serverUrl = getDefaultedProperty("FORCE_SERVER_URL", "https://login.salesforce.com/");

        AuthorizationConnector authorizationConnector = new PasswordAuthorizationConnector(username, password, serverUrl);
        recordAccessor = new JerseyRecordAccessorFactory(authorizationConnector).getRecordAccessor();
    }

    @After
    public void deleteTestObjects() {
        for (Object object : objects) {
            try {
                recordAccessor.delete(object);
            } catch (Exception e) {
                System.err.println("Failed to clean up object: " + e.toString());
            }
        }
    }

    public final RecordAccessor getRecordAccessor() {
        return recordAccessor;
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
}
