/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.CompletionHandler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.force.spa.DescribeObjectOperation;
import com.force.spa.RecordResponseException;
import com.force.spa.core.utils.CountingJsonParser;
import com.force.spa.metadata.ObjectMetadata;
import com.google.common.base.Stopwatch;

class RestDescribeObjectOperation extends AbstractRestOperation<Void, ObjectMetadata> implements DescribeObjectOperation {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
    public String toString() {
        String string = "Describe " + getName();
        if (getLogger().isDebugEnabled() && objectMetadata != null) {
            try {
                string += ": " + objectMapper.writeValueAsString(objectMetadata);
            } catch (JsonProcessingException e) {
                string += ": (Failed to re-encode object metadata as JSON)"; // Don't want a debugging exception to make real operation fail
            }
        }
        return string;
    }

    @Override
    protected void start(RestConnector connector, final Stopwatch stopwatch) {

        URI uri = URI.create("/sobjects/" + getName() + "/describe");
        connector.get(uri, new CompletionHandler<CountingJsonParser, Integer>() {
            @Override
            public void completed(CountingJsonParser parser, Integer status) {
                checkStatus(status, parser);
                RestDescribeObjectOperation.this.completed(parseResponseUsing(parser), buildStatistics(null, parser, stopwatch));
            }

            @Override
            public void failed(Throwable exception, Integer status) {
                RestDescribeObjectOperation.this.failed(exception, buildStatistics(null, null, stopwatch));
            }
        });
    }

    private ObjectMetadata parseResponseUsing(JsonParser parser) {
        try {
            objectMetadata = parser.readValueAs(ObjectMetadata.class);
            return objectMetadata;
        } catch (IOException e) {
            throw new RecordResponseException("Failed to parse JSON response", e);
        }
    }
}
