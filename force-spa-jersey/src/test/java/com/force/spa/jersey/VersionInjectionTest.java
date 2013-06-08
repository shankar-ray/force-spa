/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.ApiVersion;
import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessor;
import com.force.spa.core.rest.RestRecordAccessor;
import com.sun.jersey.api.client.Client;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath:com/force/spa/jersey/versionInjectionContext.xml"})
public class VersionInjectionTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private SpringRecordAccessorFactory accessorFactory;

    @Test
    public void testVersionInjection() {
        assertThat(accessorFactory, is(not(nullValue())));
        assertThat(accessorFactory.getApiVersion(), is(equalTo(new ApiVersion(27,0))));
    }
}
