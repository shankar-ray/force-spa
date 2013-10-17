/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import static com.force.spa.core.utils.JsonParserUtils.checkExpectedTokenThenClear;
import static com.force.spa.core.utils.JsonParserUtils.checkExpectedTokenThenNext;
import static com.force.spa.core.utils.JsonParserUtils.establishCurrentToken;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

@SuppressWarnings("UnusedDeclaration")
@JsonIgnoreProperties(ignoreUnknown = true)
class QueryResult<R> {

    private boolean done;

    private int totalSize;

    private String nextRecordsUrl;

    private List<R> records;

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public String getNextRecordsUrl() {
        return nextRecordsUrl;
    }

    public void setNextRecordsUrl(String nextRecordsUrl) {
        this.nextRecordsUrl = nextRecordsUrl;
    }

    public List<R> getRecords() {
        return records;
    }

    public void setRecords(List<R> records) {
        this.records = records;
    }

    /**
     * Manually parse a QueryResult so we can offer a type hint to overcome ambiguity in situations when there might be
     * multiple beans for the same type.
     */
    public static <R> QueryResult<R> deserialize(JsonParser parser, Class<R> recordClass) throws IOException {
        QueryResult<R> queryResult = new QueryResult<>();
        establishCurrentToken(parser);
        checkExpectedTokenThenNext(parser, JsonToken.START_OBJECT);
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            parser.nextValue();
            if (parser.getCurrentName().equals("totalSize")) {
                queryResult.setTotalSize(parser.getIntValue());
            } else if (parser.getCurrentName().equals("done")) {
                queryResult.setDone(parser.getBooleanValue());
            } else if (parser.getCurrentName().equals("nextRecordsUrl")) {
                queryResult.setNextRecordsUrl(parser.getValueAsString());
            } else if (parser.getCurrentName().equals("records")) {
                queryResult.setRecords(deserializeRecords(parser, recordClass));
            }
            parser.nextToken();
        }
        checkExpectedTokenThenClear(parser, JsonToken.END_OBJECT);
        return queryResult;
    }

    @SuppressWarnings("unchecked")
    private static <R> List<R> deserializeRecords(JsonParser parser, Class<R> recordClass) throws IOException {
        Class<R[]> resultArrayClass = (Class<R[]>) Array.newInstance(recordClass, 0).getClass();
        return Arrays.asList(parser.readValueAs(resultArrayClass));
    }
}
