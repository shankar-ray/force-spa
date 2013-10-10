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

    @Autowired
    private RecordAccessorConfig config;

    @Autowired
    private Client client;

    private RecordAccessor instance;

    public void setConfig(RecordAccessorConfig config) {
        this.config = config;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = new RecordAccessorFactory(config, client).getRecordAccessor();
    }

    @Override
    public RecordAccessor getObject() {
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return RecordAccessor.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    RecordAccessorConfig getConfig() {  // For unit tests only. Package private to protect integrity of mutable config.
        return config;
    }

    Client getClient() {                // For unit tests only. Package private to protect integrity of mutable config.
        return client;
    }
}
