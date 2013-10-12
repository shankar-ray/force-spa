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
import com.force.spa.core.MappingContext;
import com.force.spa.core.rest.RestConnector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

@ContextConfiguration(locations = {"classpath:com/force/spa/jersey/scanWithPropertyOverridesContext.xml"})
public class SpringScanWithPropertyOverridesTest extends AbstractJUnit4SpringContextTests {

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
        RecordAccessorConfig defaultRecordAccessorConfig = applicationContext.getBean("spa.recordAccessorConfig", RecordAccessorConfig.class);
        ClientConfig defaultClientConfig = applicationContext.getBean("spa.clientConfig", ClientConfig.class);
        AuthorizationConnector defaultAuthorizationConnector = applicationContext.getBean("spa.authorizationConnector", AuthorizationConnector.class);
        MappingContext defaultMappingContext = applicationContext.getBean("spa.mappingContext", MappingContext.class);
        RestConnector defaultRestConnector = applicationContext.getBean("spa.restConnector", RestConnector.class);
        RecordAccessor defaultRecordAccessor = applicationContext.getBean("spa.recordAccessor", RecordAccessor.class);

        assertThat(recordAccessorConfig, is(sameInstance(defaultRecordAccessorConfig)));
        assertThat(clientConfig, is(sameInstance(defaultClientConfig)));
        assertThat(authorizationConnector, is(sameInstance(defaultAuthorizationConnector)));
        assertThat(mappingContext, is(sameInstance(defaultMappingContext)));
        assertThat(restConnector, is(sameInstance(defaultRestConnector)));
        assertThat(recordAccessor, is(sameInstance(defaultRecordAccessor)));
    }

    @Test
    public void testClientConfigOverrides() {
        ClientConfig clientConfig = applicationContext.getBean("spa.clientConfig", ClientConfig.class);
        assertThat(clientConfig, is(sameInstance(this.clientConfig)));
        assertThat(clientConfig.getProperties(), hasEntry(ExtendedClientConfig.PROPERTY_MAX_CONNECTIONS_TOTAL, (Object) 1000));
        assertThat(clientConfig.getProperties(), hasEntry(ExtendedClientConfig.PROPERTY_MAX_CONNECTIONS_PER_ROUTE, (Object) 100));
    }

    @Test
    public void testClientOverrides() {
        Client client = applicationContext.getBean("spa.client", Client.class);
        assertThat(client.getProperties(), not(hasEntry(ExtendedClientConfig.PROPERTY_MAX_CONNECTIONS_TOTAL, (Object) 1000)));
        assertThat(client.getProperties(), not(hasEntry(ExtendedClientConfig.PROPERTY_MAX_CONNECTIONS_PER_ROUTE, (Object) 100)));
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

        assertThat(recordAccessor, is(sameInstance(this.recordAccessor)));
        assertThat(recordAccessor.getConfig(), is(sameInstance(localRecordAccessorConfig)));
    }
}
