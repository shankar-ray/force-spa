/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordAccessor;
import com.force.spa.core.MappingContext;
import com.force.spa.core.rest.RestConnector;
import com.force.spa.core.rest.RestRecordAccessorFactory;

/**
 * A Spring factory for a singleton instance of {@link RecordAccessor}.
 * <p/>
 * The record accessor is thread safe and can be shared. If you need multiple record accessors then you can configure
 * multiple instances of this factory.
 */
@Component("spa.recordAccessor")
public class SpringRestRecordAccessorFactory implements FactoryBean<RecordAccessor>, InitializingBean {

    @Autowired
    private RecordAccessorConfig config;

    @Autowired
    private MappingContext mappingContext;

    @Autowired
    private RestConnector connector;

    private RecordAccessor instance;

    public void setConfig(RecordAccessorConfig config) {
        this.config = config;
    }

    public void setMappingContext(MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    public void setConnector(RestConnector connector) {
        this.connector = connector;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = new RestRecordAccessorFactory(config, mappingContext, connector).getRecordAccessor();
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
}
