/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.io.InputStream;
import java.net.URI;
import java.nio.channels.CompletionHandler;

import com.force.spa.core.rest.RestConnector;

/**
 * A {@link com.force.spa.core.rest.RestConnector} implementation that is designed for use in unit tests. The asynchronous callback interface of
 * the RestConnector is a pain to mock so this TestRestConnector wraps some synchronous methods that are much easier to
 * mock.
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
    }

    @Override
    public final void delete(URI uri, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        try {
            delete(uri);
            completionHandler.completed(null, getStatus());
        } catch (Exception e) {
            completionHandler.failed(e, getStatus());
        }
    }

    @Override
    public final void get(URI uri, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        try {
            InputStream resultStream = get(uri);
            completionHandler.completed(getMappingContext().createParser(resultStream), getStatus());
        } catch (Exception e) {
            completionHandler.failed(e, getStatus());
        }
    }

    @Override
    public final void patch(URI uri, String jsonBody, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        try {
            patch(uri, jsonBody);
            completionHandler.completed(null, getStatus());
        } catch (Exception e) {
            completionHandler.failed(e, getStatus());
        }
    }

    @Override
    public final void post(URI uri, String jsonBody, CompletionHandler<CountingJsonParser, Integer> completionHandler) {
        try {
            InputStream resultStream = post(uri, jsonBody);
            completionHandler.completed(getMappingContext().createParser(resultStream), getStatus());
        } catch (Exception e) {
            completionHandler.failed(e, getStatus());
        }
    }
}
