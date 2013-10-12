/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessor;
import com.force.spa.jersey.JerseyRecordAccessorFactory;
import com.force.spa.jersey.PasswordAuthorizationConnector;

public class AbstractRecordAccessorIntegrationTest {

    private RecordAccessor recordAccessor;

    protected final Set<Object> objects = new HashSet<>();

    @Before
    public void setUpRecordAccessor() {
        AuthorizationConnector authorizationConnector = new PasswordAuthorizationConnector();
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
}
