/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Utilities to ease working with {@link URLEncoder}.
 */
public final class URLEncoderDecoderUtils {

    private URLEncoderDecoderUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("The silly system doesn't know about UTF-8?");
        }
    }

    public static String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("The silly system doesn't know about UTF-8?");
        }
    }
}
