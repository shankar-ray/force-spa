/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.force.spa.GetRecordOperation;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RestConnector;
import com.force.spa.core.ObjectDescriptor;
import com.force.spa.core.ObjectMappingContext;
import com.force.spa.core.SoqlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

class RestGetRecordOperation<T> extends AbstractRestRecordOperation<T> implements GetRecordOperation<T> {
    private static final Logger log = LoggerFactory.getLogger(RestGetRecordOperation.class);

    private final String id;
    private final Class<T> recordClass;

    public RestGetRecordOperation(String id, Class<T> recordClass) {
        if (id == null)
            throw new IllegalArgumentException("id must not be null");
        if (recordClass == null)
            throw new IllegalArgumentException("recordClass must not be null");

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
    public void start(RestConnector connector, final ObjectMappingContext mappingContext) {
        final ObjectDescriptor descriptor = mappingContext.getRequiredObjectDescriptor(recordClass);

        String soqlTemplate = String.format("SELECT * FROM %s WHERE Id = '%s'", descriptor.getName(), id);
        String soql = new SoqlBuilder(descriptor).soqlTemplate(soqlTemplate).limit(1).build();
        URI uri = URI.create("/query?q=" + encodeParameter(soql));

        if (log.isDebugEnabled()) {
            log.debug(String.format("Get %s %s: %s", descriptor.getName(), id, soql));
        }

        connector.get(uri, new RestConnector.Callback<JsonNode>() {
            @Override
            public void onSuccess(JsonNode result) {
                JsonNode recordNode = result.get("records").get(0);
                if (recordNode == null) {
                    throw new RecordNotFoundException();
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("...Got: %s", recordNode.toString()));
                }
                T record = decodeRecord(mappingContext, recordNode, recordClass);
                set(record);
            }

            @Override
            public void onFailure(RuntimeException exception) {
                setException(exception);
            }
        });
    }
}
