/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordResponseException;
import com.force.spa.RestConnector;
import com.force.spa.batch.CreateRecordOperation;
import com.force.spa.batch.DeleteRecordOperation;
import com.force.spa.batch.GetRecordOperation;
import com.force.spa.batch.PatchRecordOperation;
import com.force.spa.batch.QueryRecordsOperation;
import com.force.spa.batch.RecordOperationVisitor;
import com.force.spa.batch.UpdateRecordOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.Holder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link com.force.spa.RecordAccessor} that is based on the JSON representations of the Salesforce
 * REST API.
 */
public final class RestRecordOperationExecutor implements RecordOperationVisitor {

    private static final Logger log = LoggerFactory.getLogger(RestRecordOperationExecutor.class);

    private final RestConnector connector;
    private final ObjectMappingContext mappingContext;

    public RestRecordOperationExecutor(RestConnector connector, ObjectMappingContext mappingContext) {
        this.connector = connector;
        this.mappingContext = mappingContext;
    }

    @Override
    public void visit(final CreateRecordOperation operation) {
        final Object record = operation.getRecord();
        final ObjectDescriptor descriptor = getObjectDescriptor(record.getClass());

        URI uri = URI.create("/sobjects/" + descriptor.getName());
        Map<String, String> headers = determineHeaders(descriptor, record);
        String json = encodeJsonForCreate(record);

        optionallyLogRequest("Create", descriptor.getName(), null, json);

        connector.post(uri, json, headers, new RestConnector.Callback<JsonNode>() {
            @Override
            public void onSuccess(JsonNode result) {
                if (!result.has("success") || !result.has("id")) {
                    throw new RecordResponseException("JSON response is missing expected fields");
                }
                if (!result.get("success").asBoolean()) {
                    throw new RecordResponseException(getErrorsText(result));
                }
                String id = result.get("id").asText();

                if (log.isDebugEnabled()) {
                    log.debug(String.format("...Created %s %s", descriptor.getName(), id));
                }
                operation.onSuccess(id);
            }

            @Override
            public void onFailure(RuntimeException exception) {
                operation.onFailure(exception);
            }
        });
    }

    @Override
    public void visit(final DeleteRecordOperation operation) {
        final ObjectDescriptor descriptor = getObjectDescriptor(operation.getRecordClass());

        URI uri = URI.create("/sobjects/" + descriptor.getName() + "/" + operation.getId());
        Map<String, String> headers = determineHeaders(descriptor, null);

        optionallyLogRequest("Delete", descriptor.getName(), operation.getId(), null);

        connector.delete(uri, headers, new RestConnector.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("...Deleted %s %s", descriptor.getName(), operation.getId()));
                }
                operation.onSuccess();
            }

            @Override
            public void onFailure(RuntimeException exception) {
                operation.onFailure(exception);
            }
        });
    }

    @Override
    public void visit(final GetRecordOperation<?> operation) {
        final ObjectDescriptor descriptor = getObjectDescriptor(operation.getRecordClass());

        String soqlTemplate = String.format("SELECT * FROM %s WHERE Id = '%s'", descriptor.getName(), operation.getId());
        String soql = new SoqlBuilder(descriptor).soqlTemplate(soqlTemplate).limit(1).build();
        URI uri = URI.create("/query?q=" + encode(soql));
        Map<String, String> headers = determineHeaders(descriptor, null);

        if (log.isDebugEnabled())
            optionallyLogRequest("Get", descriptor.getName(), operation.getId(), soql);

        connector.get(uri, headers, new RestConnector.Callback<JsonNode>() {
            @Override
            public void onSuccess(JsonNode result) {
                JsonNode recordNode = result.get("records").get(0);
                if (recordNode == null) {
                    throw new RecordNotFoundException();
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("...Got: %s", recordNode.toString()));
                }
                Object record = decodeRecord(recordNode, operation.getRecordClass());
                operation.onSuccess(record);
            }

            @Override
            public void onFailure(RuntimeException exception) {
                operation.onFailure(exception);
            }
        });
    }

    @Override
    public void visit(final PatchRecordOperation operation) {
        final Object record = operation.getRecord();
        final ObjectDescriptor descriptor = getObjectDescriptor(record.getClass());

        URI uri = URI.create("/sobjects/" + descriptor.getName() + "/" + operation.getId());
        Map<String, String> headers = determineHeaders(descriptor, record);
        String json = encodeJsonForPatch(record);

        optionallyLogRequest("Update", descriptor.getName(), operation.getId(), json);

        connector.patch(uri, json, headers, new RestConnector.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("...Updated %s %s", descriptor.getName(), operation.getId()));
                }
                operation.onSuccess();
            }

            @Override
            public void onFailure(RuntimeException exception) {
                operation.onFailure(exception);
            }
        });
    }

    @Override
    public void visit(final QueryRecordsOperation<?> operation) {
        final ObjectDescriptor descriptor = getObjectDescriptor(operation.getRecordClass());

        String soql = new SoqlBuilder(descriptor).soqlTemplate(operation.getSoqlTemplate()).build(); //TODO handle offset and limit stuff
        URI uri = URI.create("/query?q=" + encode(soql));
        Map<String, String> headers = determineHeaders(descriptor, null);

        if (log.isDebugEnabled())
            log.debug(String.format("Query: %s", soql));

        connector.get(uri, headers, new RestConnector.Callback<JsonNode>() {
            @Override
            public void onSuccess(JsonNode result) {
                final List<Object> records = new ArrayList<Object>();
                for (JsonNode node : result.get("records")) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("...Result Row: %s", node.toString()));
                    }
                    records.add(decodeRecord(node, operation.getResultClass()));
                }

                JsonNode nextRecordsUrlNode = result.get("nextRecordsUrl");
                while (nextRecordsUrlNode != null) {
                    nextRecordsUrlNode = getNextRecords(nextRecordsUrlNode, records);
                }
                operation.onSuccess(records);
            }

            @Override
            public void onFailure(RuntimeException exception) {
                operation.onFailure(exception);
            }

            private JsonNode getNextRecords(final JsonNode nextRecordsUrlNode, final List<Object> records) {
                if (connector.isSynchronous()) {
                    final Holder<JsonNode> resultHolder = new Holder<JsonNode>();
                    URI nextRecordsUri = URI.create(nextRecordsUrlNode.asText());
                    Map<String, String> headers = determineHeaders(descriptor, null);
                    connector.get(nextRecordsUri, headers, new RestConnector.Callback<JsonNode>() {
                        @Override
                        public void onSuccess(JsonNode result) {
                            for (JsonNode node : result.get("records")) {
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("...Result Row: %s", node.toString()));
                                }
                                records.add(decodeRecord(node, operation.getResultClass()));
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

    @Override
    public void visit(final UpdateRecordOperation operation) {
        final Object record = operation.getRecord();
        final ObjectDescriptor descriptor = getObjectDescriptor(record.getClass());

        URI uri = URI.create("/sobjects/" + descriptor.getName() + "/" + operation.getId());
        Map<String, String> headers = determineHeaders(descriptor, record);
        String json = encodeJsonForUpdate(record);

        optionallyLogRequest("Update", descriptor.getName(), operation.getId(), json);

        connector.patch(uri, json, headers, new RestConnector.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("...Updated %s %s", descriptor.getName(), operation.getId()));
                }
                operation.onSuccess();
            }

            @Override
            public void onFailure(RuntimeException exception) {
                operation.onFailure(exception);
            }
        });
    }

    private ObjectDescriptor getObjectDescriptor(Class<?> clazz) {
        return mappingContext.getRequiredObjectDescriptor(clazz);
    }

    private String encodeJsonForCreate(Object record) {
        try {
            return mappingContext.getObjectWriterForCreate().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to encode record as JSON", e);
        }
    }

    private String encodeJsonForPatch(Object record) {
        try {
            return mappingContext.getObjectWriterForPatch().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to encode record as JSON", e);
        }
    }

    private String encodeJsonForUpdate(Object record) {
        try {
            return mappingContext.getObjectWriterForUpdate().writeValueAsString(record);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to encode record as JSON", e);
        }
    }

    private Object decodeRecord(JsonNode node, Class<?> recordClass) {
        if (recordClass.equals(JsonNode.class)) {
            return node;
        } else {
            ObjectReader objectReader = mappingContext.getObjectReader();
            try {
                return objectReader.readValue(objectReader.treeAsTokens(node), recordClass);
            } catch (IOException e) {
                throw new RecordResponseException("Failed to decode record from JSON", e);
            }
        }
    }

    /**
     * Determine any entity-specific headers that we want to attach to the outbound REST request to Salesforce.
     */
    private static Map<String, String> determineHeaders(ObjectDescriptor descriptor, Object record) {
        Map<String, String> headers = null;
        headers = WorkSharingOptimization.updateHeaders(descriptor, record, headers);

        if (headers != null && log.isDebugEnabled()) {
            log.debug(String.format("...With Headers: %s", headers.toString()));
        }

        return headers;
    }

    private static void optionallyLogRequest(String operation, String objectName, String id, String detail) {
        if (log.isDebugEnabled()) {
            if (id != null) {
                log.debug(String.format("%s %s %s: %s", operation, objectName, id, StringUtils.trimToEmpty(detail)));
            } else {
                log.debug(String.format("%s %s: %s", operation, objectName, StringUtils.trimToNull(detail)));
            }
        }
    }

    private static String getErrorsText(JsonNode node) {
        if (node.has("errors")) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode error : node.get("errors")) {
                if (sb.length() > 0)
                    sb.append("; ");
                sb.append(error.asText());
            }
            if (sb.length() > 0)
                return sb.toString();
        }
        return "Salesforce persistence error with no message";
    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("The silly system doesn't know about UTF-8");
        }
    }
}
