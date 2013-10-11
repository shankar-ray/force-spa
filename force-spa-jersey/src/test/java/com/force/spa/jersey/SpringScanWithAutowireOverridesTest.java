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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

@ContextConfiguration(locations = {"classpath:com/force/spa/jersey/scanWithAutowireOverridesContext.xml"})
public class SpringScanWithAutowireOverridesTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private ClientConfig clientConfig;

    @Autowired
    private Client client;

    @Autowired
    private AuthorizationConnector authorizationConnector;

    @Autowired
    private RecordAccessorConfig recordAccessorConfig;

    @Autowired
    private RecordAccessor recordAccessor;

    @Test
    public void testAutowiring() {
        ClientConfig defaultClientConfig = applicationContext.getBean("local.clientConfig", ClientConfig.class);
        AuthorizationConnector defaultAuthorizationConnector = applicationContext.getBean("local.authorizationConnector", AuthorizationConnector.class);
        RecordAccessorConfig defaultRecordAccessorConfig = applicationContext.getBean("local.recordAccessorConfig", RecordAccessorConfig.class);
        RecordAccessor defaultRecordAccessor = applicationContext.getBean("local.recordAccessor", RecordAccessor.class);

        assertThat(clientConfig, is(sameInstance(defaultClientConfig)));
        assertThat(authorizationConnector, is(sameInstance(defaultAuthorizationConnector)));
        assertThat(recordAccessorConfig, is(sameInstance(defaultRecordAccessorConfig)));
        assertThat(recordAccessor, is(sameInstance(defaultRecordAccessor)));

        assertThat(recordAccessorConfig.getAuthorizationConnector(), is(sameInstance(this.authorizationConnector)));
        assertThat(recordAccessor.getConfig(), is(sameInstance(this.recordAccessorConfig)));
    }
}
