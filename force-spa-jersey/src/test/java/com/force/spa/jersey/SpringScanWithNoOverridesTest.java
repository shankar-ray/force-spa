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

import com.force.spa.RecordAccessorConfig;
import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessor;
import com.sun.jersey.api.client.config.ClientConfig;

@ContextConfiguration(locations = {"classpath:com/force/spa/jersey/scanWithNoOverridesContext.xml"})
public class SpringScanWithNoOverridesTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private ClientConfig clientConfig;

    @Autowired
    private AuthorizationConnector authorizationConnector;

    @Autowired
    private RecordAccessorConfig recordAccessorConfig;

    @Autowired
    private RecordAccessor recordAccessor;

    @Autowired
    private RecordAccessor recordAccessor2;

    @Test
    public void testAutowiring() {
        ClientConfig defaultClientConfig = applicationContext.getBean("spa.clientConfig", ClientConfig.class);
        AuthorizationConnector defaultAuthorizationConnector = applicationContext.getBean("spa.authorizationConnector", AuthorizationConnector.class);
        RecordAccessorConfig defaultRecordAccessorConfig = applicationContext.getBean("spa.recordAccessorConfig", RecordAccessorConfig.class);
        RecordAccessor defaultRecordAccessor = applicationContext.getBean("spa.recordAccessor", RecordAccessor.class);

        assertThat(clientConfig, is(sameInstance(defaultClientConfig)));
        assertThat(authorizationConnector, is(sameInstance(defaultAuthorizationConnector)));
        assertThat(recordAccessorConfig, is(sameInstance(defaultRecordAccessorConfig)));
        assertThat(recordAccessor, is(sameInstance(defaultRecordAccessor)));
    }

    @Test
    public void testRecordAccessorIsSingleton() {
        assertThat(recordAccessor, is(sameInstance(recordAccessor2)));
    }
}
