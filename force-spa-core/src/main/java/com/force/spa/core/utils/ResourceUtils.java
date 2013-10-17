/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public final class ResourceUtils {

    private ResourceUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    /**
     * Returns the contents of the specified resource as a String.
     *
     * @param resourceName the name of the resource.
     * @return the contents of the specified resource as a String.
     * @throws java.io.IOException if the resource can not be found or could not be converted to a string.
     */
    public static String getResourceString(String resourceName) throws IOException {
        return IOUtils.toString(getResourceStream(resourceName), "UTF-8");
    }

    /**
     * Returns the contents of the specified resource as an InputStream.
     *
     * @param resourceName the name of the resource
     * @return the contents of the specified resource as an InputStream
     * @throws java.io.FileNotFoundException if the resource can not be found
     */
    public static InputStream getResourceStream(String resourceName) throws FileNotFoundException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: " + resourceName);
        }
        return inputStream;
    }
}
