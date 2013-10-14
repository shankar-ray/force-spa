/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.core.JsonParser;
import com.force.spa.DescribeObjectOperation;
import com.force.spa.ObjectNotFoundException;
import com.force.spa.core.utils.CountingJsonParser;
import com.force.spa.metadata.ObjectMetadata;

class RestDescribeObjectOperation extends AbstractRestRecordOperation<Void, ObjectMetadata> implements DescribeObjectOperation {

    private final String name;

    private ObjectMetadata objectMetadata;

    public RestDescribeObjectOperation(RestRecordAccessor accessor, String name) {
        super(accessor, null);

        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void start(RestConnector connector) {

        connector.get(URI.create("/sobjects/" + getName() + "/describe"), new ResponseHandler() {
            @Override
            public ObjectMetadata deserialize(CountingJsonParser parser) throws IOException {
                return deserializeObjectMetadata(parser);
            }

            @Override
            public void handleStatus(int status, JsonParser parser) {
                if (status == 404) {
                    throw new ObjectNotFoundException(getExceptionMessage(status, parser));
                } else {
                    super.handleStatus(status, parser);
                }
            }
        });
    }

    private ObjectMetadata deserializeObjectMetadata(JsonParser parser) throws IOException {
        objectMetadata = parser.readValueAs(ObjectMetadata.class);
        return objectMetadata;
    }

    @Override
    public String toString() {
        String string = "Describe " + getName();
        if (getLogger().isDebugEnabled() && objectMetadata != null) {
            string += ": " + objectMetadata;
        }
        return string;
    }
}
