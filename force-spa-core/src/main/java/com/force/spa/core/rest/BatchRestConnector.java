/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.force.spa.ApiVersion;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordRequestException;
import com.force.spa.RecordResponseException;
import com.force.spa.RestConnector;

public class BatchRestConnector implements RestConnector {
    private static final Logger log = LoggerFactory.getLogger(BatchRestConnector.class);

    private static final int MAX_BATCH_SIZE = 25;

    private final RestConnector connector;
    private final List<BatchRequest> pendingRequests;

    public BatchRestConnector(RestConnector connector) {
        this.connector = connector;
        this.pendingRequests = new ArrayList<BatchRequest>();
    }

    @Override
    public void delete(URI uri, Callback<Void> callback) {
        pendingRequests.add(new DeleteRequest(buildVersionedRelativeUri(uri), callback));
    }

    @Override
    public void get(URI uri, Callback<JsonNode> callback) {
        pendingRequests.add(new GetRequest(buildVersionedRelativeUri(uri), callback));
    }

    @Override
    public void patch(URI uri, String jsonBody, Callback<Void> callback) {
        pendingRequests.add(new PatchRequest(buildVersionedRelativeUri(uri), jsonBody, callback));
    }

    @Override
    public void post(URI uri, String jsonBody, Callback<JsonNode> callback) {
        pendingRequests.add(new PostRequest(buildVersionedRelativeUri(uri), jsonBody, callback));
    }

    @Override
    public ApiVersion getApiVersion() {
        return connector.getApiVersion();
    }

    @Override
    public boolean isSynchronous() {
        return false;
    }

    @Override
    public void flush() {
        flush(false);
    }

    void flush(final boolean haltOnError) {
        URI uri = URI.create("/connect/batch");
        while (pendingRequests.size() > 0) {
            final int batchSize = Math.min(pendingRequests.size(), MAX_BATCH_SIZE);
            final List<BatchRequest> batchRequests = pendingRequests.subList(0, batchSize);
            String batchJson = buildBatch(batchRequests, haltOnError);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Posting Batch of %d requests", batchSize));
            }

            connector.post(uri, batchJson, new Callback<JsonNode>() {
                @Override
                public void onSuccess(JsonNode result) {

                    if (log.isDebugEnabled()) {
                        log.debug("Batch succeeded");
                    }

                    JsonNode batchResults = result.get("results");
                    if (batchResults != null && batchResults.isArray()) {
                        for (JsonNode batchResult : batchResults) {
                            BatchRequest batchRequest = batchRequests.remove(0);
                            batchRequest.processResult(batchResult);
                        }
                    } else {
                        abort(new RecordResponseException("Failed to parse batch response stream"));
                    }
                    if (batchRequests.size() > 0) {
                        abort(new RecordResponseException("Insufficient results in batch response"));
                    }
                }

                @Override
                public void onFailure(RuntimeException exception) {
                    if (log.isDebugEnabled()) {
                        log.debug("Batch failed", exception);
                    }
                    abort(exception);
                }

                private void abort(RuntimeException exception) {
                    while (batchRequests.size() > 0) {
                        batchRequests.remove(0).processException(exception);
                    }
                    if (haltOnError) {
                        exception = new RecordResponseException("Processing skipped because of an earlier failure in the batch", exception);
                        while (pendingRequests.size() > 0) {
                            pendingRequests.remove(0).processException(exception);
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
            builder.append(batchRequest.getRequestJson());
            addComma = true;
        }
        builder.append("]}");
        return builder.toString();
    }

    private static int extractStatusCode(JsonNode batchResult) {
        JsonNode statusCode = batchResult.get("statusCode");
        if (statusCode != null) {
            return statusCode.asInt();
        } else {
            throw new RecordResponseException("Failed to parse JSON batch result, missing \"statusCode\"");
        }
    }

    private static JsonNode extractResult(JsonNode batchResult) {
        JsonNode result = batchResult.get("result");
        if (result != null) {
            return result;
        } else {
            throw new RecordResponseException("Failed to parse JSON batch result, missing \"result\"");
        }
    }

    private static String extractErrorMessage(JsonNode batchResult) {
        JsonNode result = batchResult.get("result");
        if (result != null && result.isArray()) {
            if (result.get(0).has("message")) {
                return result.get(0).get("message").asText();
            } else {
                return result.toString();
            }
        } else {
            return null;
        }
    }

    private abstract static class BatchRequest {
        private final String method;
        private final String requestJson;

        BatchRequest(String method, URI uri) {
            this.method = method;
            this.requestJson = String.format("{\"url\":\"%s\",\"method\":\"%s\"}", uri.toString(), method);
        }

        BatchRequest(String method, URI uri, String richInput) {
            this.method = method;
            this.requestJson = String.format("{\"url\":\"%s\",\"method\":\"%s\",\"richInput\": %s}", uri.toString(), method, richInput);
        }

        final String getRequestJson() {
            return requestJson;
        }

        abstract void processResult(JsonNode batchResult);

        abstract void processException(RuntimeException exception);

        void checkStatus(JsonNode batchResult) {
            int status = extractStatusCode(batchResult);
            if (status >= 300) {
                String message = String.format("%s failed: %s", method, extractErrorMessage(batchResult));
                if (status == 404) {
                    throw new RecordNotFoundException(message);
                } else {
                    throw new RecordRequestException(message);
                }
            }
        }
    }

    private static class DeleteRequest extends BatchRequest {
        private final Callback<Void> callback;

        DeleteRequest(URI uri, Callback<Void> callback) {
            super("Delete", uri);
            this.callback = callback;
        }

        @Override
        public void processResult(JsonNode batchResult) {
            try {
                checkStatus(batchResult);
                callback.onSuccess(null);

            } catch (RuntimeException e) {
                callback.onFailure(e);
            }
        }

        @Override
        void processException(RuntimeException exception) {
            callback.onFailure(exception);
        }
    }

    private static class GetRequest extends BatchRequest {
        private final Callback<JsonNode> callback;

        GetRequest(URI uri, Callback<JsonNode> callback) {
            super("Get", uri);
            this.callback = callback;
        }

        @Override
        void processResult(JsonNode batchResult) {
            try {
                checkStatus(batchResult);
                callback.onSuccess(extractResult(batchResult));

            } catch (RuntimeException e) {
                callback.onFailure(e);
            }
        }

        @Override
        void processException(RuntimeException exception) {
            callback.onFailure(exception);
        }
    }

    private static class PatchRequest extends BatchRequest {
        private final Callback<Void> callback;

        PatchRequest(URI uri, String jsonBody, Callback<Void> callback) {
            super("Patch", uri, jsonBody);
            this.callback = callback;
        }

        @Override
        public void processResult(JsonNode batchResult) {
            try {
                checkStatus(batchResult);
                callback.onSuccess(null);

            } catch (RuntimeException e) {
                callback.onFailure(e);
            }
        }

        @Override
        void processException(RuntimeException exception) {
            callback.onFailure(exception);
        }
    }

    private static class PostRequest extends BatchRequest {
        private final Callback<JsonNode> callback;

        PostRequest(URI uri, String jsonBody, Callback<JsonNode> callback) {
            super("Post", uri, jsonBody);
            this.callback = callback;
        }

        @Override
        public void processResult(JsonNode batchResult) {
            try {
                checkStatus(batchResult);
                callback.onSuccess(extractResult(batchResult));

            } catch (RuntimeException e) {
                callback.onFailure(e);
            }
        }

        @Override
        void processException(RuntimeException exception) {
            callback.onFailure(exception);
        }
    }
}
