/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.force.spa.AuthorizationConnector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * A Spring factory for instances of Jersey {@link Client} configured appropriately for JerseyRestConnector use.
 * <p/>
 * This factory creates instances of {@link com.sun.jersey.client.apache4.ApacheHttpClient4} because the Apache client
 * is needed to support the HTTP "PATCH" request required by the Salesforce API. If you decide not to use this factory
 * and instead provide a client instance of your own then make sure that the client instance can support HTTP "PATCH".
 * You really should use this factory or something derived from it.
 * <p/>
 * By default, the returned instances use a {@link org.apache.http.impl.conn.PoolingClientConnectionManager} in order to
 * support multi-threaded use.
 */
@Component("clientFactory")
public class SpringClientFactory implements FactoryBean<Client>, InitializingBean {

    private ClientFactory delegate;

    @Autowired
    private AuthorizationConnector authorizationConnector;

    @Autowired(required = false)
    private ClientConfig clientConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
        delegate = (clientConfig != null) ? new ClientFactory(clientConfig) : new ClientFactory();
    }

    @Override
    public Client getObject() throws Exception {
        return delegate.newInstance(authorizationConnector);
    }

    @Override
    public Class<?> getObjectType() {
        return Client.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
