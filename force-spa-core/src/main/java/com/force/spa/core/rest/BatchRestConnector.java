/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import static com.force.spa.core.utils.JsonParserUtils.consumeExpectedToken;
import static com.force.spa.core.utils.JsonParserUtils.establishCurrentToken;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.force.spa.ApiVersion;
import com.force.spa.RecordResponseException;
import com.force.spa.Statistics;
import com.force.spa.core.utils.CountingJsonParser;

/**
 * Note: Some of the complexity of result handling is caused by the fact that the result is returned by the server
 * before we see the status. This complicates our life if we want to be efficient and stream the result parsing.
 */
class BatchRestConnector implements RestConnector {

    private static final Logger LOG = LoggerFactory.getLogger(BatchRestConnector.class);

    private static final int MAX_BATCH_SIZE = 25; // Hardcoded limit in Salesforce core

    private final RestConnector innerConnector;
    private final List<Request> pendingRequests;

    public BatchRestConnector(RestConnector innerConnector) {
        this.innerConnector = innerConnector;
        this.pendingRequests = new ArrayList<>();
    }

    @Override
    public void delete(URI uri, RestResponseHandler<Void> responseHandler) {
        pendingRequests.add(new DeleteRequest(toVersionedRelativeUri(uri), responseHandler));
    }

    @Override
    public <R> void get(URI uri, RestResponseHandler<R> responseHandler) {
        pendingRequests.add(new GetRequest<>(toVersionedRelativeUri(uri), responseHandler));
    }

    @Override
    public void patch(URI uri, String jsonBody, RestResponseHandler<Void> responseHandler) {
        pendingRequests.add(new PatchRequest(toVersionedRelativeUri(uri), jsonBody, responseHandler));
    }

    @Override
    public <R> void post(URI uri, String jsonBody, RestResponseHandler<R> responseHandler) {
        pendingRequests.add(new PostRequest<>(toVersionedRelativeUri(uri), jsonBody, responseHandler));
    }

    @Override
    public URI getInstanceUrl() {
        return innerConnector.getInstanceUrl();
    }

    @Override
    public ApiVersion getApiVersion() {
        return innerConnector.getApiVersion();
    }

    @Override
    public boolean isSynchronous() {
        return false;
    }

    @Override
    public void join() {
        join(false);
    }

    void join(final boolean haltOnError) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Starting to process %d pending requests", pendingRequests.size()));
        }

        final Holder<Integer> numberOfFailures = new Holder<>(0);
        final Holder<Integer> numberOfSuccesses = new Holder<>(0);
        while ((pendingRequests.size() > 0) && !(numberOfFailures.value > 0 && haltOnError)) {
            final int batchSize = Math.min(pendingRequests.size(), MAX_BATCH_SIZE);
            final List<Request> batchRequests = pendingRequests.subList(0, batchSize);
            String jsonBody = toBatchJson(batchRequests, haltOnError);

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Posting batch of %d requests", batchSize));
            }

            innerConnector.post(URI.create("/connect/batch"), jsonBody, new RestResponseHandler<Void>() {
                @Override
                public Void deserialize(CountingJsonParser parser) throws IOException {
                    return deserializeBatchResult(parser, batchRequests);
                }

                @Override
                public void completed(Void ignored, Statistics statistics) {
                    long elapsedNanosPerRequest = statistics.getElapsedNanos() / batchSize;
                    while (batchRequests.size() > 0) {
                        Request request = batchRequests.remove(0);
                        try {
                            request.handleStatus();
                            numberOfSuccesses.value = numberOfSuccesses.value + 1;
                            request.completed(null, elapsedNanosPerRequest);
                        } catch (Exception e) {
                            numberOfFailures.value = numberOfFailures.value + 1;
                            request.failed(e, elapsedNanosPerRequest);
                        }
                    }
                }

                @Override
                public void failed(Throwable exception, Statistics statistics) {
                    long elapsedNanosPerRequest = statistics.getElapsedNanos() / batchSize;
                    exception = new RecordResponseException("Entire batch failed", exception);
                    while (batchRequests.size() > 0) {
                        numberOfFailures.value = numberOfFailures.value + 1;
                        batchRequests.remove(0).failed(exception, elapsedNanosPerRequest);
                    }
                }
            });
            innerConnector.join();
        }

        // Under successful circumstances there should be nothing left. If there is, fail them all.
        Exception exception = new RecordResponseException("Request skipped because of an earlier failure in the batch");
        while (pendingRequests.size() > 0) {
            numberOfFailures.value = numberOfFailures.value + 1;
            pendingRequests.remove(0).failed(exception, 0L);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Processed %d requests with %d failures",
                (numberOfSuccesses.value + numberOfFailures.value), numberOfFailures.value));
        }
    }

    private URI toVersionedRelativeUri(URI uri) {
        String path = uri.getPath();
        if (!path.startsWith("/services/data")) {
            path = "/services/data/v" + getApiVersion() + path;
        }
        try {
            return new URI(null, null, null, 0, path, uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("A URI built this way should never have a syntax exception");
        }
    }

    private static String toBatchJson(List<Request> requests, boolean haltOnError) {
        StringBuilder builder = new StringBuilder(256);
        builder.append("{\"haltOnError\":").append(haltOnError);
        builder.append(",\"batchRequests\":[");
        boolean addComma = false;
        for (Request request : requests) {
            if (addComma) {
                builder.append(',');
            }
            builder.append(request.toJson());
            addComma = true;
        }
        builder.append("]}");
        return builder.toString();
    }

    private Void deserializeBatchResult(CountingJsonParser parser, List<Request> requests) throws IOException {
        boolean hasErrors = false;
        establishCurrentToken(parser);
        consumeExpectedToken(parser, JsonToken.START_OBJECT);
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            parser.nextValue();
            if (parser.getCurrentName().equals("hasErrors")) {
                hasErrors = parser.getBooleanValue();
                parser.nextToken();
            } else if (parser.getCurrentName().equals("results")) {
                deserializeResults(parser, requests, hasErrors);
            } else {
                parser.nextToken(); // Ignore field value we don't care about
            }
        }
        consumeExpectedToken(parser, JsonToken.END_OBJECT);
        return null;
    }

    private static void deserializeResults(CountingJsonParser parser, List<Request> requests, boolean batchHasErrors) throws IOException {
        consumeExpectedToken(parser, JsonToken.START_ARRAY);
        Iterator<Request> requestCursor = requests.iterator();
        while (parser.getCurrentToken() == JsonToken.START_OBJECT) {
            if (!requestCursor.hasNext()) {
                throw new JsonParseException("Too many batch results were returned", parser.getCurrentLocation());
            }
            deserializeResult(parser, requestCursor.next(), batchHasErrors);
        }
        consumeExpectedToken(parser, JsonToken.END_ARRAY);

        if (requestCursor.hasNext()) {
            throw new JsonParseException("Not enough batch results were returned", parser.getCurrentLocation());
        }
    }

    private static void deserializeResult(CountingJsonParser parser, Request request, boolean batchHasErrors) throws IOException {
        consumeExpectedToken(parser, JsonToken.START_OBJECT);
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            parser.nextValue();
            if (parser.getCurrentName().equals("statusCode")) {
                request.deserializeAndSaveStatus(parser);
            } else if (parser.getCurrentName().equals("result")) {
                request.deserializeAndSaveResult(parser, batchHasErrors);
            }
            establishCurrentToken(parser);
        }
        consumeExpectedToken(parser, JsonToken.END_OBJECT);
    }

    private static class Request<R> implements CompletionHandler<Void, Long> {

        private final String method;
        private final URI uri;
        private final String jsonBody;
        private final RestResponseHandler<R> responseHandler;

        private R result;
        private int status;
        private long bytesSent;
        private long bytesReceived;
        private JsonParser deferredResultParser;

        Request(String method, URI uri, String jsonBody, RestResponseHandler<R> responseHandler) {
            this.method = method;
            this.uri = uri;
            this.jsonBody = jsonBody;
            this.responseHandler = responseHandler;
            this.result = null;
            this.deferredResultParser = null;
        }

        final String toJson() {
            bytesSent = (jsonBody != null) ? jsonBody.length() : 0;
            return String.format("{\"url\":\"%s\",\"method\":\"%s\",\"richInput\": %s}", uri.toString(), method, jsonBody);
        }

        final void deserializeAndSaveResult(CountingJsonParser parser, boolean batchHasErrors) throws IOException {
            JsonLocation startLocation = parser.getCurrentLocation();
            if (batchHasErrors) {
                // Save for later inspection in case this entry has an error. (This is less efficient so we try to avoid).
                deferredResultParser = parser.getCodec().treeAsTokens(parser.readValueAsTree());
            } else {
                // With no errors to worry about we can do things simpler and faster!
                result = responseHandler.deserialize(parser);
            }
            bytesReceived = CountingJsonParser.differenceBetween(startLocation, parser.getCurrentLocation());
        }

        final void deserializeAndSaveStatus(CountingJsonParser parser) throws IOException {
            status = parser.getIntValue();
            parser.nextToken();
        }

        final void handleStatus() throws IOException {
            if (deferredResultParser != null) {
                responseHandler.handleStatus(status, deferredResultParser);
                result = responseHandler.deserialize(new CountingJsonParser(deferredResultParser));
            } else {
                responseHandler.handleStatus(status, null);
            }
        }

        @Override
        public final void completed(Void ignored, Long elapsedNanos) {
            responseHandler.completed(result, buildStatistics(elapsedNanos));
        }

        @Override
        public final void failed(Throwable exception, Long elapsedNanos) {
            try {
                responseHandler.failed(mapSelectedExceptions(exception), buildStatistics(elapsedNanos));
            } catch (Exception e) {
                LOG.error("Request failed during failure processing", e);
            }
        }

        private Statistics buildStatistics(long elapsedNanos) {
            return new Statistics.Builder().bytesSent(bytesSent).bytesReceived(bytesReceived).elapsedNanos(elapsedNanos).build();
        }

        private static Throwable mapSelectedExceptions(Throwable exception) {
            if (exception instanceof JsonParseException) {
                return new RecordResponseException("Failed to parse response body", exception);
            } else {
                return exception;
            }
        }
    }

    private static class DeleteRequest extends Request<Void> {
        DeleteRequest(URI uri, RestResponseHandler<Void> responseHandler) {
            super("Delete", uri, null, responseHandler);
        }
    }

    private static class GetRequest<R> extends Request<R> {
        GetRequest(URI uri, RestResponseHandler<R> responseHandler) {
            super("Get", uri, null, responseHandler);
        }
    }

    private static class PatchRequest extends Request<Void> {
        PatchRequest(URI uri, String jsonBody, RestResponseHandler<Void> responseHandler) {
            super("Patch", uri, jsonBody, responseHandler);
        }
    }

    private static class PostRequest<R> extends Request<R> {
        PostRequest(URI uri, String jsonBody, RestResponseHandler<R> responseHandler) {
            super("Post", uri, jsonBody, responseHandler);
        }
    }
}
