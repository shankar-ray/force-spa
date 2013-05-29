/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.force.spa.RecordResponseException;
import com.force.spa.core.AbstractRecordOperation;
import com.force.spa.core.ObjectDescriptor;
import com.force.spa.core.ObjectMappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public abstract class AbstractRestRecordOperation<T> extends AbstractRecordOperation<T> implements RestRecordOperation<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractRestRecordOperation.class);

    /**
     * Determine any entity-specific headers that we want to attach to the outbound REST request to Salesforce.
     */
    protected Map<String, String> determineHeaders(ObjectDescriptor descriptor, Object record) {
        Map<String, String> headers = null;
        headers = WorkSharingOptimization.updateHeaders(descriptor, record, headers);

        if (headers != null && log.isDebugEnabled()) {
            log.debug(String.format("...With Headers: %s", headers.toString()));
        }

        return headers;
    }

    protected static String encodeParameter(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("The silly system doesn't know about UTF-8?");
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T> T decodeRecord(ObjectMappingContext mappingContext, JsonNode node, Class<T> recordClass) {
        if (recordClass.equals(JsonNode.class)) {
            return (T) node;
        } else {
            ObjectReader objectReader = mappingContext.getObjectReader();
            try {
                return objectReader.readValue(objectReader.treeAsTokens(node), recordClass);
            } catch (IOException e) {
                throw new RecordResponseException("Failed to decode record from JSON", e);
            }
        }
    }
}
