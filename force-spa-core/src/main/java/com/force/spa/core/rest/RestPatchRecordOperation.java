/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.force.spa.PatchRecordOperation;
import com.force.spa.RecordResponseException;
import com.force.spa.RestConnector;
import com.force.spa.core.ObjectDescriptor;

class RestPatchRecordOperation<T> extends AbstractRestRecordOperation<Void> implements PatchRecordOperation<T> {
    private static final Logger log = LoggerFactory.getLogger(RestPatchRecordOperation.class);

    private final String id;
    private final T record;

    public RestPatchRecordOperation(RestRecordAccessor accessor, String id, T record) {
        super(accessor);

        Validate.notEmpty(id, "id must not be empty");
        Validate.notNull(record, "record must not be null");

        this.id = id;
        this.record = record;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public T getRecord() {
        return record;
    }

    @Override
    public void start(RestConnector connector) {
        final ObjectDescriptor descriptor = getObjectMappingContext().getRequiredObjectDescriptor(record.getClass());

        URI uri = URI.create("/sobjects/" + descriptor.getName() + "/" + id);
        String json = encodeRecordForPatch(record);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Update %s %s: %s", descriptor.getName(), id, json));
        }

        connector.patch(uri, json, new RestConnector.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("...Updated %s %s", descriptor.getName(), id));
                }
                set(null);
            }

            @Override
            public void onFailure(RuntimeException exception) {
                setException(exception);
            }
        });
    }

    private String encodeRecordForPatch(Object record) {
        try {
            return getObjectMappingContext().getObjectWriterForPatch().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to encode record as JSON", e);
        }
    }
}
