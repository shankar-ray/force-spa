/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.MappingContext;
import com.force.spa.core.rest.RestConnector;
import com.sun.jersey.api.client.config.ClientConfig;

@ContextConfiguration(locations = {"classpath:com/force/spa/jersey/scanWithAutowireOverridesContext.xml"})
public class SpringScanWithAutowireOverridesTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private RecordAccessorConfig recordAccessorConfig;

    @Autowired
    private ClientConfig clientConfig;

    @Autowired
    private AuthorizationConnector authorizationConnector;

    @Autowired
    private MappingContext mappingContext;

    @Autowired
    private RestConnector restConnector;

    @Autowired
    private RecordAccessor recordAccessor;

    @Test
    public void testAutowiring() {
        RecordAccessorConfig localRecordAccessorConfig = applicationContext.getBean("local.recordAccessorConfig", RecordAccessorConfig.class);
        ClientConfig localClientConfig = applicationContext.getBean("local.clientConfig", ClientConfig.class);
        AuthorizationConnector localAuthorizationConnector = applicationContext.getBean("local.authorizationConnector", AuthorizationConnector.class);
        MappingContext localMappingContext = applicationContext.getBean("local.mappingContext", MappingContext.class);
        RestConnector localRestConnector = applicationContext.getBean("local.restConnector", RestConnector.class);
        RecordAccessor localRecordAccessor = applicationContext.getBean("local.recordAccessor", RecordAccessor.class);

        assertThat(recordAccessorConfig, is(sameInstance(localRecordAccessorConfig)));
        assertThat(clientConfig, is(sameInstance(localClientConfig)));
        assertThat(authorizationConnector, is(sameInstance(localAuthorizationConnector)));
        assertThat(mappingContext, is(sameInstance(localMappingContext)));
        assertThat(restConnector, is(sameInstance(localRestConnector)));
        assertThat(recordAccessor, is(sameInstance(localRecordAccessor)));

        assertThat(recordAccessorConfig.getAuthorizationConnector(), is(sameInstance(this.authorizationConnector)));
        assertThat(recordAccessor.getConfig(), is(sameInstance(this.recordAccessorConfig)));
    }
}
