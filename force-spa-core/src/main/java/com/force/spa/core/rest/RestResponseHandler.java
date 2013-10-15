/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import static com.force.spa.core.utils.JsonParserUtils.establishCurrentToken;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordRequestException;
import com.force.spa.Statistics;
import com.force.spa.UnauthorizedException;
import com.force.spa.core.utils.CountingJsonParser;

/**
 * A handler that is invoked to help at a couple key points during response processing.
 * <p/>
 * Ideally this class wouldn't exist and we'd just have a completion handler but subtleties of the server batching
 * response layout require some additional handling before completion. This class helps with that.
 * <p/>
 * Default implements of many of the methods exist to handle the most common cases.
 */
public abstract class RestResponseHandler<R> implements CompletionHandler<R, Statistics> {

    private static final Logger LOG = LoggerFactory.getLogger(RestResponseHandler.class);

    /**
     * Called to deserialize a response body.
     * <p/>
     * In general, this method should return JsonParseException for most failures. Other kinds of errors can be better
     * thrown from the {@link #handleStatus} method or {@link #completed}.
     */
    public R deserialize(CountingJsonParser parser) throws IOException {
        switch (parser.getCurrentToken()) {
            case START_OBJECT:
            case START_ARRAY:
                parser.skipChildren();
                break;

            default:
                parser.nextToken();
                break;
        }
        return null;
    }

    /**
     * Called to handle an HTTP response status.
     * <p/>
     * The default implementation handles many common cases. One reason you may want to override this is if you want to
     * return a unique exception for a particular status value.
     */
    public void handleStatus(int status, JsonParser parser) {
        if (status >= 300) {
            switch (status) {
                case 401:
                    throw new UnauthorizedException(getExceptionMessage(status, parser));

                case 404:
                    throw new RecordNotFoundException(getExceptionMessage(status, parser));

                default:
                    throw new RecordRequestException(getExceptionMessage(status, parser));
            }
        }
    }

    /**
     * Called to handle an HTTP response status for methods that have no response body.
     * <p/>
     * This is just a convenience wrapper for {@link #handleStatus(int, JsonParser)}. To modify behavior you should
     * override {@link #handleStatus(int, JsonParser)} instead.
     */
    public final void handleStatus(int status) {
        handleStatus(status, null);
    }

    /**
     * Called to extract an exception message from a response.
     * <p/>
     * The default implementation handles the standard Salesforce REST error response. You only need to override this
     * method if you have a unique error response to parse.
     */
    public String getExceptionMessage(int status, JsonParser parser) {
        if (parser != null) {
            try {
                boolean entryAppended = false;
                StringBuilder builder = new StringBuilder(120);
                for (ErrorResult errorResult : deserializeErrorResults(parser)) {
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
                }
            } catch (Exception e) {
                LOG.error("Failed to parser error response", e);
            }
        }
        return "HTTP response status: " + status;
    }

    private static List<ErrorResult> deserializeErrorResults(JsonParser parser) throws IOException {
        establishCurrentToken(parser);
        switch (parser.getCurrentToken()) {
            case START_OBJECT:
                return Collections.singletonList(parser.readValueAs(ErrorResult.class));

            case START_ARRAY:
                return Arrays.asList(parser.readValueAs(ErrorResult[].class));

            default:
                return Collections.emptyList();
        }
    }
}
