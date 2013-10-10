/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.force.spa.ApiVersion;
import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordAccessorConfig;

/**
 * A Spring bean for creating {@link RecordAccessorConfig} instances.
 *
 * @see RecordAccessorConfig
 */
@Component("spa.recordAccessorConfig")
public class SpringRecordAccessorConfigFactory implements FactoryBean<RecordAccessorConfig> {

    private RecordAccessorConfig recordAccessorConfig = RecordAccessorConfig.DEFAULT;

    @Autowired
    public void setAuthorizationConnector(AuthorizationConnector authorizationConnector) {
        recordAccessorConfig = recordAccessorConfig.withAuthorizationConnector(authorizationConnector);
    }

    public void setApiVersion(String apiVersion) {
        recordAccessorConfig = recordAccessorConfig.withApiVersion(new ApiVersion(apiVersion));
    }

    public void setAuditFieldWritingAllowed(boolean auditFieldWritingAllowed) {
        recordAccessorConfig = recordAccessorConfig.withAuditFieldWritingAllowed(auditFieldWritingAllowed);
    }

    public void setFieldAnnotationRequired(boolean fieldAnnotationRequired) {
        recordAccessorConfig = recordAccessorConfig.withFieldAnnotationRequired(fieldAnnotationRequired);
    }

    public void setObjectAnnotationRequired(boolean objectAnnotationRequired) {
        recordAccessorConfig = recordAccessorConfig.withObjectAnnotationRequired(objectAnnotationRequired);
    }

    @Override
    public RecordAccessorConfig getObject() throws Exception {
        return recordAccessorConfig;
    }

    @Override
    public Class<?> getObjectType() {
        return RecordAccessorConfig.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
