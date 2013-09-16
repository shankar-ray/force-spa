/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.net.URI;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.force.spa.DeleteRecordOperation;
import com.force.spa.RestConnector;
import com.force.spa.core.ObjectDescriptor;

class RestDeleteRecordOperation<T> extends AbstractRestRecordOperation<Void> implements DeleteRecordOperation<T> {
    private static final Logger log = LoggerFactory.getLogger(RestDeleteRecordOperation.class);

    private final String id;
    private final Class<T> recordClass;

    public RestDeleteRecordOperation(RestRecordAccessor accessor, String id, Class<T> recordClass) {
        super(accessor);

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");

        this.id = id;
        this.recordClass = recordClass;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Class<T> getRecordClass() {
        return recordClass;
    }

    @Override
    public void start(RestConnector connector) {
        final ObjectDescriptor descriptor = getObjectMappingContext().getRequiredObjectDescriptor(recordClass);

        URI uri = URI.create("/sobjects/" + descriptor.getName() + "/" + id);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Delete %s %s", descriptor.getName(), id));
        }

        connector.delete(uri, new RestConnector.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("...Deleted %s %s", descriptor.getName(), id));
                }
                set(null);
            }

            @Override
            public void onFailure(RuntimeException exception) {
                setException(exception);
            }
        });
    }
}
