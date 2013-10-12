/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.MappingContext;
import com.force.spa.core.rest.RestConnector;
import com.force.spa.jersey.JerseyRestConnectorFactory;
import com.sun.jersey.api.client.Client;

/**
 * A Spring factory for a singleton instance of {@link com.force.spa.jersey.JerseyRestConnector}.
 * <p/>
 * The record accessor is thread safe and can be shared. If you need multiple record accessors then you can configure
 * multiple instances of this factory.
 */
@Component("spa.restConnector")
public class SpringRestConnectorFactory implements FactoryBean<RestConnector>, InitializingBean {

    @Autowired
    private RecordAccessorConfig config;

    @Autowired
    private MappingContext mappingContext;

    @Autowired
    private Client client;

    private RestConnector instance;

    public void setConfig(RecordAccessorConfig config) {
        this.config = config;
    }

    public void setMappingContext(MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = new JerseyRestConnectorFactory(config, mappingContext, client).getRestConnector();
    }

    @Override
    public RestConnector getObject() {
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return RestConnector.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
