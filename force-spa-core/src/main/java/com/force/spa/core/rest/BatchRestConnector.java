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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import com.force.spa.ApiVersion;
import com.force.spa.RecordResponseException;
import com.force.spa.core.CountingJsonParser;

//TODO Maybe stopwatch has to be thread local to make all this batch time division stuff to work? That seems reasonable.

public class BatchRestConnector implements RestConnector {

    private static final Logger LOG = LoggerFactory.getLogger(BatchRestConnector.class);

    private static final int MAX_BATCH_SIZE = 25; // Hardcoded limit in Salesforce core

    private final RestConnector baseConnector;
    private final List<BatchRequest> pendingRequests;

    public BatchRestConnector(RestConnector baseConnector) {
        this.baseConnector = baseConnector;
        this.pendingRequests = new ArrayList<BatchRequest>();
    }

    @Override
    public void delete(URI uri, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        pendingRequests.add(new BatchRequest("Delete", buildVersionedRelativeUri(uri), null, completionHandler));
    }

    @Override
    public void get(URI uri, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        pendingRequests.add(new BatchRequest("Get", buildVersionedRelativeUri(uri), null, completionHandler));
    }

    @Override
    public void patch(URI uri, String jsonBody, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        pendingRequests.add(new BatchRequest("Patch", buildVersionedRelativeUri(uri), jsonBody, completionHandler));
    }

    @Override
    public void post(URI uri, String jsonBody, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        pendingRequests.add(new BatchRequest("Post", buildVersionedRelativeUri(uri), jsonBody, completionHandler));
    }

    @Override
    public URI getInstanceUrl() {
        return baseConnector.getInstanceUrl();
    }

    @Override
    public ApiVersion getApiVersion() {
        return baseConnector.getApiVersion();
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
        while (pendingRequests.size() > 0) {
            final int batchSize = Math.min(pendingRequests.size(), MAX_BATCH_SIZE);
            final List<BatchRequest> batchRequests = pendingRequests.subList(0, batchSize);
            String batchJson = buildBatch(batchRequests, haltOnError);

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Posting Batch of %d requests", batchSize));
            }

            URI uri = URI.create("/connect/batch");
            baseConnector.post(uri, batchJson, new CompletionHandler<CountingJsonParser, Integer>() {
                @Override
                public void completed(CountingJsonParser parser, Integer status) {
                    try {
                        establishCurrentToken(parser);
                        processBatchResponse(parser, batchRequests);
                    } catch (IOException e) {
                        abort(new RecordResponseException(e), status);
                    }
                }

                @Override
                public void failed(Throwable exception, Integer status) {
                    abort(exception, status);
                }

                private void abort(Throwable exception, Integer status) {
                    while (batchRequests.size() > 0) {
                        batchRequests.remove(0).getCompletionHandler().failed(exception, status);
                    }
                    if (haltOnError) {
                        exception = new RecordResponseException("`Request skipped because of an earlier failure in the batch", exception);
                        while (pendingRequests.size() > 0) {
                            pendingRequests.remove(0).getCompletionHandler().failed(exception, status);
                        }
                    }
                }
            });
        }
    }

    private URI buildVersionedRelativeUri(URI uri) {
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

    private String buildBatch(List<BatchRequest> batchRequests, boolean haltOnError) {
        StringBuilder builder = new StringBuilder(256);
        builder.append("{\"haltOnError\":").append(haltOnError);
        builder.append(",\"batchRequests\":[");
        boolean addComma = false;
        for (BatchRequest batchRequest : batchRequests) {
            if (addComma) {
                builder.append(',');
            }
            builder.append(batchRequest.getJson());
            addComma = true;
        }
        builder.append("]}");
        return builder.toString();
    }

    private static void processBatchResponse(CountingJsonParser parser, List<BatchRequest> batchRequests) throws IOException {
        consumeExpectedToken(parser, JsonToken.START_OBJECT);
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            parser.nextValue();
            if (parser.getCurrentName().equals("results")) {
                processBatchResults(parser, batchRequests);
            } else {
                parser.nextToken(); // Ignore field value we don't care about
            }
        }
        consumeExpectedToken(parser, JsonToken.END_OBJECT);
    }

    private static void processBatchResults(CountingJsonParser parser, List<BatchRequest> batchRequests) throws IOException {
        consumeExpectedToken(parser, JsonToken.START_ARRAY);
        while (parser.getCurrentToken() == JsonToken.START_OBJECT) {
            if (batchRequests.size() == 0) {
                throw new JsonParseException("Too many batch results were returned", parser.getCurrentLocation());
            }
            processBatchResult(parser, batchRequests.remove(0));
        }
        consumeExpectedToken(parser, JsonToken.END_ARRAY);

        if (batchRequests.size() != 0) {
            throw new JsonParseException("Not enough batch results were returned", parser.getCurrentLocation());
        }
    }

    private static void processBatchResult(CountingJsonParser parser, BatchRequest batchRequest) throws IOException {
        int statusCode = 0;
        boolean resultProcessed = false;
        consumeExpectedToken(parser, JsonToken.START_OBJECT);
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            parser.nextValue();
            if (parser.getCurrentName().equals("statusCode")) {
                statusCode = parser.getIntValue();
                parser.nextToken();
            } else if (parser.getCurrentName().equals("result")) {
                resultProcessed = true;
                batchRequest.getCompletionHandler().completed(parser, statusCode);
                establishCurrentToken(parser);
            } else {
                parser.nextToken(); // Ignore field value we don't care about
            }
        }

        if (!resultProcessed) {
            batchRequest.getCompletionHandler().completed(parser, statusCode); // Must be a batched request with no response body
        }

        consumeExpectedToken(parser, JsonToken.END_OBJECT);
    }

    private static class BatchRequest {

        private final String method;
        private final URI uri;
        private final String richInput;
        private final CompletionHandler<CountingJsonParser, Integer> completionHandler;

        BatchRequest(String method, URI uri, String richInput, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
            this.method = method;
            this.uri = uri;
            this.richInput = richInput;
            this.completionHandler = completionHandler;
        }

        final String getJson() {
            return String.format("{\"url\":\"%s\",\"method\":\"%s\",\"richInput\": %s}", uri.toString(), method, richInput);
        }

        private CompletionHandler<CountingJsonParser, Integer> getCompletionHandler() {
            return completionHandler;
        }
    }
}
