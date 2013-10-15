/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonParseException;
import com.force.spa.RecordResponseException;
import com.force.spa.Statistics;
import com.force.spa.core.rest.RestConnector;
import com.force.spa.core.rest.RestResponseHandler;
import com.force.spa.core.utils.CountingJsonParser;
import com.google.common.base.Stopwatch;

/**
 * A {@link com.force.spa.core.rest.RestConnector} implementation that is designed for use in unit tests. The
 * asynchronous callback interface of the RestConnector is a pain to mock so this TestRestConnector wraps some
 * synchronous methods that are much easier to mock.
 */
public abstract class TestRestConnector implements RestConnector {

    public abstract void delete(URI uri);

    public abstract InputStream get(URI uri);

    public abstract void patch(URI uri, String jsonBody);

    public abstract InputStream post(URI uri, String jsonBody);

    public abstract MappingContext getMappingContext();

    @SuppressWarnings("SameReturnValue")
    public int getStatus() {
        return 200;
    }

    @Override
    public final boolean isSynchronous() {
        return true;
    }

    @Override
    public final void join() {
        // Nothing to do for a synchronous connector.
    }

    @Override
    public final void delete(URI uri, RestResponseHandler<Void> responseHandler) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            delete(uri);
            responseHandler.handleStatus(getStatus(), null);
            responseHandler.completed(null, buildStatistics(stopwatch, null, null));
        } catch (Exception e) {
            responseHandler.failed(mapSelectedExceptions(e), buildStatistics(stopwatch, null, null));
        }
    }

    @Override
    public final <T> void get(URI uri, RestResponseHandler<T> responseHandler) {
        CountingJsonParser parser = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            InputStream resultStream = get(uri);
            parser = getMappingContext().createParser(resultStream);
            int status = getStatus();
            responseHandler.handleStatus(status, parser);
            T result = (status < 300) ? responseHandler.deserialize(parser) : null;
            responseHandler.completed(result, buildStatistics(stopwatch, null, parser));
        } catch (Exception e) {
            responseHandler.failed(mapSelectedExceptions(e), buildStatistics(stopwatch, null, parser));
        }
    }

    @Override
    public final void patch(URI uri, String jsonBody, RestResponseHandler<Void> responseHandler) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            patch(uri, jsonBody);
            responseHandler.handleStatus(getStatus(), null);
            responseHandler.completed(null, buildStatistics(stopwatch, jsonBody, null));
        } catch (Exception e) {
            responseHandler.failed(mapSelectedExceptions(e), buildStatistics(stopwatch, jsonBody, null));
        }
    }

    @Override
    public final <T> void post(URI uri, String jsonBody, RestResponseHandler<T> responseHandler) {
        CountingJsonParser parser = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            InputStream resultStream = post(uri, jsonBody);
            parser = getMappingContext().createParser(resultStream);
            int status = getStatus();
            responseHandler.handleStatus(status, parser);
            T result = (status < 300) ? responseHandler.deserialize(parser) : null;
            responseHandler.completed(result, buildStatistics(stopwatch, jsonBody, parser));
        } catch (Exception e) {
            responseHandler.failed(mapSelectedExceptions(e), buildStatistics(stopwatch, null, parser));
        }
    }

    private Statistics buildStatistics(Stopwatch stopwatch, String jsonBody, CountingJsonParser parser) {
        return new Statistics.Builder()
            .bytesSent((jsonBody != null) ? jsonBody.length() : 0)
            .bytesReceived((parser != null) ? parser.getCount() : 0)
            .elapsedNanos(stopwatch.elapsed(TimeUnit.NANOSECONDS))
            .build();
    }

    private static Exception mapSelectedExceptions(Exception exception) {
        if (exception instanceof JsonParseException) {
            return new RecordResponseException("Failed to parse response body", exception);
        } else {
            return exception;
        }
    }
}
