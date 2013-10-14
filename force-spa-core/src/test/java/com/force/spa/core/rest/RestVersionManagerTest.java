/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Test;

import com.force.spa.ApiVersion;
import com.force.spa.SpaException;

public class RestVersionManagerTest extends AbstractRestRecordAccessorTest {

    @Test
    public void testGetHighestSupportedVersion() throws Exception {

        when(connector.get(any(URI.class)))
            .thenReturn(getResourceStream("versionsResponse.json"));

        RestVersionManager versionManager = new RestVersionManager(connector);
        assertThat(versionManager.getHighestSupportedVersion(), is(equalTo(new ApiVersion("29.0"))));

        verify(connector, times(1)).get(URI.create("/services/data"));
    }

    @Test
    public void testGetHighestSupportedVersionIsCached() throws Exception {

        when(connector.get(any(URI.class)))
            .thenReturn(getResourceStream("versionsResponse.json"));

        RestVersionManager versionManager = new RestVersionManager(connector);
        assertThat(versionManager.getHighestSupportedVersion(), is(equalTo(new ApiVersion("29.0"))));
        assertThat(versionManager.getHighestSupportedVersion(), is(equalTo(new ApiVersion("29.0"))));

        verify(connector, times(1)).get(URI.create("/services/data"));
    }

    @Test
    public void testGetHighestSupportedVersionNoStream() {
        RestVersionManager versionManager = new RestVersionManager(connector);
        try {
            versionManager.getHighestSupportedVersion();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(SpaException.class)));
            assertThat(e.getMessage(), is(not(equalTo(e.getCause().getMessage()))));
        }
    }

    @Test
    public void testGetHighestSupportedVersionBadResponse() throws Exception {

        when(connector.get(any(URI.class)))
            .thenReturn(getResourceStream("badVersionsResponse.json"));

        RestVersionManager versionManager = new RestVersionManager(connector);
        assertThat(versionManager.getHighestSupportedVersion(), is(equalTo(RestVersionManager.DEFAULT_API_VERSION)));
    }

    @Test
    public void testGetHighestSupportedVersionEmptyResponse() throws Exception {

        when(connector.get(any(URI.class)))
            .thenReturn(getResourceStream("badVersionsResponse.json"));

        RestVersionManager versionManager = new RestVersionManager(connector);
        assertThat(versionManager.getHighestSupportedVersion(), is(equalTo(RestVersionManager.DEFAULT_API_VERSION)));
    }
}
