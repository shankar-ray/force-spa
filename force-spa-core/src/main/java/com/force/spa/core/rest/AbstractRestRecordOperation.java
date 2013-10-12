/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import static com.force.spa.core.utils.JsonParserUtils.consumeExpectedToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.force.spa.OperationStatistics;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordRequestException;
import com.force.spa.UnauthorizedException;
import com.force.spa.core.AbstractRecordOperation;
import com.force.spa.core.utils.CountingJsonParser;
import com.google.common.base.Stopwatch;

/**
 * @param <T> the type of record the operation is working with
 * @param <R> the type of result expected from the operation
 */
public abstract class AbstractRestRecordOperation<T, R> extends AbstractRecordOperation<T, R> {

    protected AbstractRestRecordOperation(RestRecordAccessor accessor, Class<T> recordClass) {
        super(accessor, recordClass);
    }

    @Override
    public RestRecordAccessor getRecordAccessor() {
        return (RestRecordAccessor) super.getRecordAccessor();
    }

    /**
     * Starts the (potentially asynchronous) operation.
     *
     * @param connector a REST connector
     * @param stopwatch a started stopwatch
     */
    protected abstract void start(RestConnector connector, Stopwatch stopwatch);

    /**
     * Simple convenience routine for building statistics.
     */
    protected static OperationStatistics buildStatistics(String requestBody, CountingJsonParser parser, Stopwatch stopwatch) {
        return new OperationStatistics.Builder()
            .bytesSent((requestBody != null) ? requestBody.length() : 0)
            .bytesReceived((parser != null) ? parser.getCount() : 0)
            .elapsedNanos(stopwatch.elapsed(TimeUnit.NANOSECONDS))
            .build();
    }

    protected final void checkStatus(int status, JsonParser parser) {
        if (status >= 300) {
            throw exceptionFor(status, parser);
        }
    }

    protected RuntimeException exceptionFor(int status, JsonParser parser) {
        switch (status) {
            case 401:
                return new UnauthorizedException(buildErrorMessage(status, parser));

            case 404:
                return new RecordNotFoundException(buildErrorMessage(status, parser));

            default:
                return new RecordRequestException(buildErrorMessage(status, parser));
        }
    }

    protected static String buildErrorMessage(int status, JsonParser parser) {
        try {
            boolean entryAppended = false;
            StringBuilder builder = new StringBuilder(120);
            for (ErrorResult errorResult : parseErrorResults(parser)) {
                if (entryAppended) {
                    builder.append("; ");
                }

                boolean somethingAppendedForThisEntry = false;
                if (errorResult.getErrorCode() != null) {
                    builder.append(errorResult.getErrorCode());
                    somethingAppendedForThisEntry = true;
                }

                if (errorResult.getMessage() != null) {
                    if (somethingAppendedForThisEntry) {
                        builder.append(": ");
                    }
                    builder.append(errorResult.getMessage());
                    somethingAppendedForThisEntry = true;
                }

                if (errorResult.getFields() != null) {
                    if (somethingAppendedForThisEntry) {
                        builder.append(": ");
                    }
                    builder.append("[");
                    builder.append(StringUtils.join(errorResult.getFields(), ","));
                    builder.append("]");
                }
                entryAppended = true;
            }

            if (entryAppended) {
                return builder.toString();
            } else {
                return "HTTP response status: " + status;
            }
        } catch (Exception e) {
            // Don't let an error parsing message to obscure the original condition. Ignore this exception and return generic message
            return "HTTP response status: " + status;
        }
    }

    private static List<ErrorResult> parseErrorResults(JsonParser parser) throws IOException {
        List<ErrorResult> errorResults = new ArrayList<>();
        switch (parser.getCurrentToken()) {
            case START_OBJECT:
                errorResults.add(parser.readValueAs(ErrorResult.class));
                break;

            case START_ARRAY:
                consumeExpectedToken(parser, JsonToken.START_ARRAY);
                Iterator<ErrorResult> it = parser.readValuesAs(ErrorResult.class);
                while (it.hasNext()) {
                    errorResults.add(it.next());
                }
                consumeExpectedToken(parser, JsonToken.END_ARRAY);
                break;

            default:
                break;
        }
        return errorResults;
    }
}
