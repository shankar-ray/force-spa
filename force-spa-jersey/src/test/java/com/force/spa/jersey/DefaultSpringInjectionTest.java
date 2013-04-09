/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.core.AuthorizationConnector;
import com.force.spa.core.RecordAccessor;
import com.sun.jersey.api.client.Client;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath:com/force/spa/jersey/defaultSpringInjectionContext.xml"})
public class DefaultSpringInjectionTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private AuthorizationConnector authorizationConnector;

    @Autowired
    private Client client;

    @Autowired
    private RecordAccessor accessor;

    @Test
    public void testAutowiring() {
        assertNotNull(authorizationConnector);
        assertNotNull(client);
        assertNotNull(accessor);

        assertTrue(
            "Should be instance of SpringHeaderAuthorizationConnector",
            authorizationConnector instanceof SpringHeaderAuthorizationConnector);
    }
}
