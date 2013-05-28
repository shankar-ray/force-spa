/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessor;
import com.sun.jersey.api.client.Client;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A Spring factory for instances of {@link RecordAccessor} that use a {@link JerseyRestConnector} for communications.
 */
@Component("recordAccessorFactory")
public class SpringRecordAccessorFactory implements FactoryBean<RecordAccessor> {

    private final RecordAccessorFactory internalFactory = new RecordAccessorFactory();

    @Autowired
    private Client client;

    @Autowired
    private AuthorizationConnector authorizationConnector;

    private String apiVersion = null;

    /**
     * Sets the Salesforce API version used by the generated {@link RecordAccessor} instances.
     * <p/>
     * If a version is not configured with this method then the default is to use the highest version supported by the
     * Salesforce server.
     *
     * @param apiVersion a Salesforce API version (for example: "v28.0")
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public RecordAccessor getObject() {
        return internalFactory.newInstance(authorizationConnector, client, apiVersion);
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
