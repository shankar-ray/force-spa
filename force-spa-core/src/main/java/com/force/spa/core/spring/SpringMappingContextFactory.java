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
import com.force.spa.core.MappingContext;
import com.force.spa.core.MappingContextFactory;

/**
 * A Spring factory for a singleton instance of {@link com.force.spa.core.MappingContext} tied to a particular
 * configuration.
 * <p/>
 * The mapping context is thread safe and can be shared. If you need multiple mapping contexts then you can configure
 * multiple instances of this factory.
 */
@Component("spa.mappingContext")
public class SpringMappingContextFactory implements FactoryBean<MappingContext>, InitializingBean {

    @Autowired
    private RecordAccessorConfig config;

    private MappingContext instance;

    public void setConfig(RecordAccessorConfig config) {
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = new MappingContextFactory(config).getMappingContext();
    }

    @Override
    public MappingContext getObject() {
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return MappingContext.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    RecordAccessorConfig getConfig() {  // For unit tests only.
        return config;
    }
}
