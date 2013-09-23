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

import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.sun.jersey.api.client.Client;

/**
 * A Spring factory for instances of {@link RecordAccessor} that use a {@link JerseyRestConnector} for communications.
 */
@Component("spa.recordAccessor")
public class SpringRecordAccessorFactory implements FactoryBean<RecordAccessor>, InitializingBean {

    private RecordAccessorFactory delegate;

    @Autowired
    private RecordAccessorConfig config;

    @Autowired
    private Client client;

    public void setConfig(RecordAccessorConfig config) {
        this.config = config;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        delegate = new RecordAccessorFactory(config, client);
    }

    @Override
    public RecordAccessor getObject() {
        return delegate.getRecordAccessor();
    }

    @Override
    public Class<?> getObjectType() {
        return RecordAccessor.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    RecordAccessorConfig getConfig() {
        return config;
    }

    Client getClient() {
        return client;
    }
}
