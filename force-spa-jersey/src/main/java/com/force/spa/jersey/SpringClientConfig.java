/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

/**
 * A Spring bean for configuring the Jersey {@link DefaultApacheHttpClient4Config} that used by {@link
 * SpringClientFactory} for create Jersey {@link com.sun.jersey.api.client.Client} instances.
 *
 * @see SpringClientFactory
 */
@Component("clientConfig")
public class SpringClientConfig extends DefaultApacheHttpClient4Config implements InitializingBean {

    private Map<String, Object> properties = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (properties != null) {
            getProperties().putAll(properties);
        }
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
