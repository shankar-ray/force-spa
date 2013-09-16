/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.force.spa.RecordResponseException;
import com.force.spa.core.AbstractRecordOperation;

public abstract class AbstractRestRecordOperation<T> extends AbstractRecordOperation<T> implements RestRecordOperation<T> {

    protected AbstractRestRecordOperation(RestRecordAccessor accessor) {
        super(accessor);
    }

    protected RestRecordAccessor getAccessor() {
        return (RestRecordAccessor) super.getAccessor();
    }

    @SuppressWarnings("unchecked")
    protected <T> T decodeRecord(JsonNode node, Class<T> type) {
        if (type.equals(JsonNode.class)) {
            return (T) node;
        } else {
            ObjectReader objectReader = getObjectMappingContext().getObjectReader();
            try {
                return objectReader.readValue(objectReader.treeAsTokens(node), type);
            } catch (IOException e) {
                throw new RecordResponseException("Failed to decode JSON record", e);
            }
        }
    }

    protected static String encodeParameter(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("The silly system doesn't know about UTF-8?");
        }
    }
}
