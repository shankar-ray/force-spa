/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.junit.Before;

import com.force.spa.ApiVersion;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.MappingContext;
import com.force.spa.core.TestRestConnector;

/**
 * An abstract base class that includes a small amount of supporting infrastructure to help with unit tests that need
 * a {@link TestRestConnector} or a {@link RestRecordAccessor} that is based on a {@link TestRestConnector}.
 *
 * The {@link TestRestConnector} provides unit test support for emulating REST I/O through the use of mocking and
 * resource files.
 */
public abstract class AbstractRestRecordAccessorTest {
    private final String resourcePrefix = this.getClass().getPackage().getName().replace('.', '/');

    protected MappingContext mappingContext;
    protected TestRestConnector connector;
    protected RecordAccessor accessor;

    @Before
    public void initializeConnectorAndAccessor() {
        mappingContext = new MappingContext();

        connector = mock(TestRestConnector.class);
        when(
            connector.getMappingContext())
            .thenReturn(mappingContext);
        when(
            connector.getApiVersion())
            .thenReturn(new ApiVersion("29.0"));
        when(
            connector.getInstanceUrl())
            .thenReturn(URI.create("https://na4.salesforce.com"));

        accessor = new RestRecordAccessor(new RecordAccessorConfig(), mappingContext, connector);
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
