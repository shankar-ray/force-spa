/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Holder;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordResponseException;
import com.force.spa.RestConnector;
import com.force.spa.core.ObjectDescriptor;

final class RestQueryRecordsOperation<T> extends AbstractRestRecordOperation<List<T>> implements QueryRecordsOperation<T> {
    private static final Logger log = LoggerFactory.getLogger(RestQueryRecordsOperation.class);

    private final String soqlTemplate;
    private final Class<?> recordClass;
    private final Class<T> resultClass;
    private int startPosition;
    private int maxResults;

    RestQueryRecordsOperation(RestRecordAccessor accessor, String soqlTemplate, Class<?> recordClass, Class<T> resultClass) {
        super(accessor);

        Validate.notEmpty(soqlTemplate, "template must not be empty");
        Validate.notNull(recordClass, "recordClass must not be null");
        Validate.notNull(resultClass, "resultClass must not be null");

        this.soqlTemplate = soqlTemplate;
        this.resultClass = resultClass;
        this.recordClass = recordClass;
        this.startPosition = 0;
        this.maxResults = 0;
    }

    RestQueryRecordsOperation(RestRecordAccessor accessor, String soql, Class<T> recordClass) {
        this(accessor, soql, recordClass, recordClass);
    }

    @Override
    public String getSoqlTemplate() {
        return soqlTemplate;
    }

    @Override
    public Class<?> getRecordClass() {
        return recordClass;
    }

    @Override
    public Class<T> getResultClass() {
        return resultClass;
    }

    @Override
    public int getStartPosition() {
        return startPosition;
    }

    @Override
    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    public void start(final RestConnector connector) {
        final ObjectDescriptor descriptor = getObjectMappingContext().getRequiredObjectDescriptor(recordClass);

        String soql = newSoqlBuilder().object(descriptor).template(soqlTemplate).offset(startPosition).limit(maxResults).build();
        URI uri = URI.create("/query?q=" + encodeParameter(soql));

        if (log.isDebugEnabled())
            log.debug(String.format("Query: %s", soql));

        connector.get(uri, new RestConnector.Callback<JsonNode>() {
            @Override
            public void onSuccess(JsonNode result) {
                final List<T> records = new ArrayList<T>();
                for (JsonNode node : result.get("records")) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("...Result Row: %s", node.toString()));
                    }
                    records.add(decodeRecord(node, resultClass));
                }

                JsonNode nextRecordsUrlNode = result.get("nextRecordsUrl");
                while (nextRecordsUrlNode != null) {
                    nextRecordsUrlNode = getNextRecords(nextRecordsUrlNode, records);
                }
                set(records);
            }

            @Override
            public void onFailure(RuntimeException exception) {
                setException(exception);
            }

            private JsonNode getNextRecords(final JsonNode nextRecordsUrlNode, final List<T> records) {
                if (connector.isSynchronous()) {
                    final Holder<JsonNode> resultHolder = new Holder<JsonNode>();
                    URI nextRecordsUri = URI.create(nextRecordsUrlNode.asText());
                    connector.get(nextRecordsUri, new RestConnector.Callback<JsonNode>() {
                        @Override
                        public void onSuccess(JsonNode result) {
                            for (JsonNode node : result.get("records")) {
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("...Result Row: %s", node.toString()));
                                }
                                records.add(decodeRecord(node, resultClass));
                            }
                            resultHolder.value = result.get("nextRecordsUrl");
                        }

                        @Override
                        public void onFailure(RuntimeException exception) {
                            throw exception;
                        }
                    });
                    return resultHolder.value;
                } else {
                    throw new RecordResponseException(
                        "Too many result rows. Can't fetch additional results with asynchronous connector");
                }
            }
        });
    }
}
