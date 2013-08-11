/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.RestConnector;
import com.force.spa.core.rest.RestRecordAccessor;
import org.apache.commons.io.IOUtils;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.mock;

/**
 * An abstract base class that includes a small amount of supporting infrastructure to help with unit tests related to a
 * {@link com.force.spa.core.rest.RestRecordAccessor}.
 * <p/>
 * This class initializes a {@link com.force.spa.core.rest.RestRecordAccessor} with a mock {@link RestConnector} that can be used to interact
 * with the network inputs and outputs of the test.
 * <p/>
 * Also included are some utility routines for reading mock network requests and responses from resources.
 */
public abstract class AbstractRestRecordAccessorTest {
    private final String resourcePrefix = this.getClass().getPackage().getName().replace('.', '/');

    protected RecordAccessor accessor;
    protected TestRestConnector mockConnector;

    @Before
    public void initializeMockEntityManager() {
        mockConnector = mock(TestRestConnector.class);
        accessor = new RestRecordAccessor(new RecordAccessorConfig(), mockConnector);
    }

    /**
     * Returns the contents of the specified resource as a String.
     *
     * @param relativeResourceName the name of the resource relative to the package of the current class.
     * @return the contents of the specified resource as a String.
     * @throws IOException if the resource can not be found or could not be converted to a string.
     */
    protected String getResourceString(String relativeResourceName) throws IOException {
        return IOUtils.toString(getResourceStream(relativeResourceName), "UTF-8");
    }

    /**
     * Returns the contents of the specified resource as an InputStream.
     *
     * @param relativeResourceName the name of the resource relative to the package of the current class.
     * @return the contents of the specified resource as an InputStream
     * @throws FileNotFoundException if the resource can not be found
     */
    protected InputStream getResourceStream(String relativeResourceName) throws FileNotFoundException {
        String resourceName = resourcePrefix + '/' + relativeResourceName;
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new FileNotFoundException(String.format("Testing resource not found: %s", resourceName));
        }
        return inputStream;
    }
}
