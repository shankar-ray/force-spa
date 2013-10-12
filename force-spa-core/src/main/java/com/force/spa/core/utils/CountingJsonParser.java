/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

/**
 * A wrapper for {@link JsonParser} that helps figure out how many bytes are processed between any two locations in the
 * JSON input.
 */
public class CountingJsonParser extends JsonParserDelegate {

    private JsonLocation referenceLocation;

    public CountingJsonParser(JsonParser delegate) {
        super(delegate);
        referenceLocation = getCurrentLocation();
    }

    /**
     * Returns the number of bytes that have been parsed since the last {@link #resetCount()}, or if {@link
     * #resetCount()} has not been called, since the creation of the {@link CountingJsonParser} instance.
     *
     * @return the number of bytes that have been parsed
     */
    public final long getCount() {
        return differenceBetween(referenceLocation, getCurrentLocation());
    }

    /**
     * Resets the count of bytes that have been parsed so far.
     */
    public final void resetCount() {
        referenceLocation = getCurrentLocation();
    }

    /**
     * Returns the number of bytes between two {@link JsonLocation}s.
     *
     * @param startLocation the starting location
     * @param endLocation   the ending location
     *
     * @return the number of bytes between the two locations.
     */
    public static long differenceBetween(JsonLocation startLocation, JsonLocation endLocation) {
        return endLocation.getCharOffset() - startLocation.getCharOffset();
    }
}
