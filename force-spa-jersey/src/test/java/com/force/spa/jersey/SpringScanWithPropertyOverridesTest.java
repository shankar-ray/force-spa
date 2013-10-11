/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.force.spa.ApiVersion;
import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

@ContextConfiguration(locations = {"classpath:com/force/spa/jersey/scanWithPropertyOverridesContext.xml"})
public class SpringScanWithPropertyOverridesTest extends AbstractJUnit4SpringContextTests {

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
    public void testClientConfigOverrides() {
        ClientConfig clientConfig = applicationContext.getBean("spa.clientConfig", ClientConfig.class);
        assertThat(clientConfig, is(sameInstance(this.clientConfig)));
        assertThat(clientConfig.getProperties(), hasEntry(SpaClientConfig.PROPERTY_MAX_CONNECTIONS_TOTAL, (Object) 1000));
        assertThat(clientConfig.getProperties(), hasEntry(SpaClientConfig.PROPERTY_MAX_CONNECTIONS_PER_ROUTE, (Object) 100));
    }

    @Test
    public void testClientOverrides() {
        Client client = applicationContext.getBean("spa.client", Client.class);
        assertThat(client.getProperties(), not(hasEntry(SpaClientConfig.PROPERTY_MAX_CONNECTIONS_TOTAL, (Object) 1000)));
        assertThat(client.getProperties(), not(hasEntry(SpaClientConfig.PROPERTY_MAX_CONNECTIONS_PER_ROUTE, (Object) 100)));
    }

    @Test
    public void testRecordAccessorConfigOverrides() {
        RecordAccessorConfig recordAccessorConfig = applicationContext.getBean("spa.recordAccessorConfig", RecordAccessorConfig.class);

        AuthorizationConnector localAuthorizationConnector = applicationContext.getBean("local.authorizationConnector", AuthorizationConnector.class);
        assertThat(localAuthorizationConnector, is(not(sameInstance(this.authorizationConnector))));

        assertThat(recordAccessorConfig, is(sameInstance(this.recordAccessorConfig)));
        assertThat(recordAccessorConfig.getAuthorizationConnector(), is(sameInstance(localAuthorizationConnector)));
        assertThat(recordAccessorConfig.getApiVersion(), is(equalTo(new ApiVersion("28.0"))));
        assertThat(recordAccessorConfig.isAuditFieldWritingAllowed(), is(equalTo(true)));
        assertThat(recordAccessorConfig.isFieldAnnotationRequired(), is(equalTo(true)));
        assertThat(recordAccessorConfig.isObjectAnnotationRequired(), is(equalTo(true)));
    }

    @Test
    public void testRecordAccessorOverrides() {
        RecordAccessor recordAccessor = applicationContext.getBean("spa.recordAccessor", RecordAccessor.class);

        RecordAccessorConfig localRecordAccessorConfig = applicationContext.getBean("local.recordAccessorConfig", RecordAccessorConfig.class);
        assertThat(localRecordAccessorConfig, is(not(sameInstance(this.recordAccessorConfig))));
        Client localClient = applicationContext.getBean("local.client", Client.class);
        assertThat(localClient, is(not(sameInstance(this.client))));

        assertThat(recordAccessor, is(sameInstance(this.recordAccessor)));
        assertThat(recordAccessor.getConfig(), is(sameInstance(localRecordAccessorConfig)));
    }
}
