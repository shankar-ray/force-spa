/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public final class JsonParserUtils {

    private JsonParserUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    public static void establishCurrentToken(JsonParser parser) throws IOException {
        if (!parser.hasCurrentToken()) {
            parser.nextToken();
        }
    }

    public static void checkExpectedTokenThenNext(JsonParser parser, JsonToken token) throws IOException {
        if (parser.getCurrentToken() != token) {
            throw new JsonParseException("Didn't find expected " + token, parser.getCurrentLocation());
        } else {
            parser.nextToken();
        }
    }

    public static void checkExpectedTokenThenClear(JsonParser parser, JsonToken token) throws IOException {
        if (parser.getCurrentToken() != token) {
            throw new JsonParseException("Didn't find expected " + token, parser.getCurrentLocation());
        } else {
            parser.clearCurrentToken();
        }
    }
}
