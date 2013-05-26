/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordQuery;
import com.force.spa.RecordRequestException;
import com.force.spa.RecordResponseException;
import com.force.spa.RestConnector;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link RecordAccessor} that is based on the JSON representations of the Salesforce REST API.
 */
public final class RestRecordAccessor implements RecordAccessor {

    private static final Logger log = LoggerFactory.getLogger(RestRecordAccessor.class);

    // Just one mapping context is shared by all instances. It is thread-safe and configured the same every time. There
    // is no reason to go through the expense of creating multiple instances. This way we get to share the cache.
    private static final ObjectMappingContext mappingContext = new ObjectMappingContext();

    private RestConnector connector;

    public RestRecordAccessor(RestConnector connector) {
        this.connector = connector;
    }

    @Override
    public String create(Object record) {
        Validate.notNull(record, "record must not be null");

        ObjectDescriptor descriptor = getRequiredDescriptor(record.getClass());
        String json = encodeJsonForCreate(record);

        optionallyLogRequest("Create", descriptor.getName(), null, json);

        URI uri = URI.create("/sobjects/" + descriptor.getName());
        InputStream responseStream = connector.post(uri, json, determineHeaders(descriptor, record));
        JsonNode responseNode = decodeJson(responseStream);
        if (!responseNode.has("success") || !responseNode.has("id")) {
            throw new RecordResponseException("JSON response is missing expected fields");
        }
        if (!responseNode.get("success").asBoolean()) {
            throw new RecordResponseException(getErrorsText(responseNode));
        }
        String id = responseNode.get("id").asText();

        if (log.isDebugEnabled()) {
            log.debug(String.format("...Created %s %s", descriptor.getName(), id));
        }
        return id;
    }

    @Override
    public <T> T get(String id, Class<T> recordClass) {
        Validate.notNull(id, "id must not be null");
        Validate.notNull(recordClass, "resultClass must not be null");

        ObjectDescriptor descriptor = getRequiredDescriptor(recordClass);

        optionallyLogRequest("Get", descriptor.getName(), id, null);

        return get(descriptor, id, recordClass);
    }

    @Override
    public void update(Object record) {
        Validate.notNull(record, "record must not be null");

        update(getRecordId(record), record);
    }

    @Override
    public void update(String id, Object record) {
        Validate.notNull(id, "id must not be null");
        Validate.notNull(record, "record must not be null");

        ObjectDescriptor descriptor = getRequiredDescriptor(record.getClass());
        String json = encodeJsonForUpdate(record);

        optionallyLogRequest("Update", descriptor.getName(), id, json);

        URI uri = URI.create("/sobjects/" + descriptor.getName() + "/" + id);
        connector.patch(uri, json, determineHeaders(descriptor, record));

        if (log.isDebugEnabled()) {
            log.debug(String.format("...Updated %s %s", descriptor.getName(), id));
        }
    }

    @Override
    public void patch(String id, Object recordChanges) {
        Validate.notNull(id, "id must not be null");
        Validate.notNull(recordChanges, "recordChanges must not be null");

        ObjectDescriptor descriptor = getRequiredDescriptor(recordChanges.getClass());
        String json = encodeJsonForPatch(recordChanges);

        optionallyLogRequest("Patch", descriptor.getName(), id, json);

        URI uri = URI.create("/sobjects/" + descriptor.getName() + "/" + id);
        connector.patch(uri, json, determineHeaders(descriptor, recordChanges));

        if (log.isDebugEnabled()) {
            log.debug(String.format("...Patched %s %s", descriptor.getName(), id));
        }
    }

    @Override
    public void delete(Object record) {
        Validate.notNull(record, "record must not be null");

        delete(getRecordId(record), record.getClass());
    }

    @Override
    public void delete(String id, Class<?> recordClass) {
        Validate.notNull(id, "id must not be null");
        Validate.notNull(id, "resultClass must not be null");

        ObjectDescriptor descriptor = getRequiredDescriptor(recordClass);

        optionallyLogRequest("Delete", descriptor.getName(), id, null);

        URI uri = URI.create("/sobjects/" + descriptor.getName() + "/" + id);
        connector.delete(uri, determineHeaders(descriptor, null));

        if (log.isDebugEnabled()) {
            log.debug(String.format("...Deleted %s %s", descriptor.getName(), id));
        }
    }

    @Override
    public <T> RecordQuery<T> createQuery(final String soqlTemplate, final Class<T> recordClass) {
        Validate.notNull(soqlTemplate, "soqlTemplate must not be null");
        Validate.notNull(recordClass, "resultClass must not be null");

        final ObjectDescriptor descriptor = getRequiredDescriptor(recordClass);

        optionallyLogRequest("CreateQuery", descriptor.getName(), null, soqlTemplate);

        return createQuery(descriptor, soqlTemplate, recordClass);
    }

    private ObjectDescriptor getRequiredDescriptor(Class<?> clazz) {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(clazz);
        if (descriptor == null) {
            throw new IllegalArgumentException(
                String.format("%s can't be used as a record, probably because it isn't annotated", clazz.getName()));
        }
        return descriptor;
    }

    /**
     * Get the "id" field of a record.
     * <p/>
     * This would normally be private but is made public to help with bridging the old Simplejpa interface to this new
     * interface.
     *
     * @param record the record
     */
    public String getRecordId(Object record) {
        ObjectDescriptor descriptor = getRequiredDescriptor(record.getClass());
        if (descriptor.hasIdMember()) {
            String id = RecordUtils.getId(descriptor, record);
            if (StringUtils.isEmpty(id)) {
                throw new RecordRequestException("Record bean does not have an id value set");
            }
            return id;
        } else {
            throw new RecordRequestException("Record class doesn't have an id member");
        }
    }

    /**
     * Set the "id" field of a record.
     * <p/>
     * This only exists to help with bridging the old Simplejpa interface to this new interface.
     *
     * @param record the record
     * @param id     the id
     */
    public void setRecordId(Object record, String id) {
        ObjectDescriptor descriptor = getRequiredDescriptor(record.getClass());
        if (descriptor.hasIdMember()) {
            RecordUtils.setId(descriptor, record, id);
        } else {
            throw new RecordRequestException("Record class doesn't have an id member");
        }
    }

    private <T> T get(ObjectDescriptor descriptor, String id, Class<T> recordClass) {
        String soqlTemplate = String.format("SELECT * FROM %s WHERE Id = '%s'", descriptor.getName(), id);
        List<T> records = createQuery(descriptor, soqlTemplate, recordClass).setMaxResults(1).execute();
        return records.size() > 0 ? records.get(0) : null;
    }

    private <T> RecordQuery<T> createQuery(final ObjectDescriptor descriptor, final String soqlTemplate, final Class<T> recordClass) {
        return new RestRecordQuery<T>(descriptor, soqlTemplate, recordClass);
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

    private JsonNode decodeJson(InputStream inputStream) {
        try {
            return mappingContext.getObjectReader().readTree(inputStream);
        } catch (IOException e) {
            throw new RecordResponseException("Failed to parse JSON response stream", e);
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

    private final class RestRecordQuery<T> extends AbstractRecordQuery<T> {
        private ObjectDescriptor descriptor;
        private Class<T> resultClass;
        private String soqlTemplate;

        private RestRecordQuery(ObjectDescriptor descriptor, String soqlTemplate, Class<T> resultClass) {
            this.descriptor = descriptor;
            this.resultClass = resultClass;
            this.soqlTemplate = soqlTemplate;
        }

        @Override
        public List<T> execute() {
            return execute(resultClass);
        }

        @Override
        public <R> List<R> execute(Class<R> resultClass) {
            List<R> results = new ArrayList<R>();
            try {
                String soql = new SoqlBuilder(descriptor)
                    .soqlTemplate(soqlTemplate)
                    .offset(getFirstResult())
                    .limit(getMaxResults())
                    .build();

                if (log.isDebugEnabled())
                    log.debug(String.format("...Query: %s", soql));

                // Issue the query and parse the first batch of results.

                URI uri = URI.create("/query?q=" + URLEncoder.encode(soql, "UTF-8"));
                InputStream responseStream = connector.get(uri, determineHeaders(descriptor, null));
                ObjectReader objectReader = mappingContext.getObjectReader();
                JsonNode rootNode = objectReader.readTree(responseStream);
                for (JsonNode node : rootNode.get("records")) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("...Result Row: %s", node.toString()));
                    }
                    if (resultClass.equals(JsonNode.class)) {
                        results.add((resultClass.cast(node)));
                    } else {
                        results.add(objectReader.readValue(objectReader.treeAsTokens(node), resultClass));
                    }
                }

                // Request additional results if they exist
                while (rootNode.get("nextRecordsUrl") != null) {
                    URI nextRecordsUrl = URI.create(rootNode.get("nextRecordsUrl").asText());
                    responseStream = connector.get(nextRecordsUrl, determineHeaders(descriptor, null));
                    rootNode = objectReader.readTree(responseStream);
                    for (JsonNode node : rootNode.get("records")) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("...Result Row: %s", node.toString()));
                        }
                        if (resultClass.equals(JsonNode.class)) {
                            results.add((resultClass.cast(node)));
                        } else {
                            results.add(objectReader.readValue(objectReader.treeAsTokens(node), resultClass));
                        }
                    }
                }
            } catch (IOException e) {
                throw new RecordResponseException("Failed to parse the 'query' result", e);
            }
            return results;
        }
    }
}
