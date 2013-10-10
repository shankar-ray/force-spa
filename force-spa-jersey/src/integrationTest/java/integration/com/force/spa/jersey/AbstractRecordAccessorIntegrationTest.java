/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.jersey.PasswordAuthorizationConnector;
import com.force.spa.jersey.RecordAccessorFactory;

public class AbstractRecordAccessorIntegrationTest {
    private static final SecureRandom secureRandom = new SecureRandom(RecordAccessorIntegrationTest.class.getName().getBytes());

    protected RecordAccessor accessor;
    protected final Set<Object> objects = new HashSet<>();

    @Before
    public void setUpRecordAccessor() {
        PasswordAuthorizationConnector authorizationConnector = new PasswordAuthorizationConnector();
        RecordAccessorConfig config = new RecordAccessorConfig().withAuthorizationConnector(authorizationConnector);
        this.accessor = new RecordAccessorFactory(config).getRecordAccessor();
    }

    @After
    public void deleteTestObjects() {
        for (Object object : objects) {
            try {
                accessor.delete(object);
            } catch (Exception e) {
                System.err.println("Failed to clean up object: " + e.toString());
            }
        }
    }

    protected String generateUniqueNumber() {
        return Integer.toHexString(secureRandom.nextInt());
    }
}
