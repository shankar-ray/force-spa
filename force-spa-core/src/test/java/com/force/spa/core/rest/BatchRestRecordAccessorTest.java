/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.force.spa.Operation;
import com.force.spa.RecordRequestException;
import com.force.spa.core.testbeans.SimpleBean;

public class BatchRestRecordAccessorTest extends AbstractRestRecordAccessorTest {

    @Test
    public void testTwoCreates() throws Throwable {
        try {
            SimpleBean bean1 = new SimpleBean();
            bean1.setName("Name 1");
            bean1.setDescription("Description 1");
            Operation<String> createOperation1 = accessor.newCreateRecordOperation(bean1);

            SimpleBean bean2 = new SimpleBean();
            bean2.setName("Name 2");
            bean2.setDescription("Description 2");
            Operation<String> createOperation2 = accessor.newCreateRecordOperation(bean2);

            when(
                connector.post(any(URI.class), anyString()))
                .thenReturn(getResourceStream("twoCreatesResponse.json"));

            accessor.execute(createOperation1, createOperation2);

            assertThat(createOperation1.get(), is(equalTo("a01i00000000001AAC")));
            assertThat(createOperation2.get(), is(equalTo("a01i00000000002AAC")));

            verify(connector).post(URI.create("/connect/batch"), getResourceString("twoCreatesRequest.json"));
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testTwoCreatesWithError() throws Throwable {
        try {
            SimpleBean bean1 = new SimpleBean();
            bean1.setName("Name 1");
            bean1.setDescription("Description 1");
            Operation<String> createOperation1 = accessor.newCreateRecordOperation(bean1);

            SimpleBean bean2 = new SimpleBean();
            bean2.setName("Name 2");
            bean2.setDescription("Description 2");
            Operation<String> createOperation2 = accessor.newCreateRecordOperation(bean2);

            when(
                connector.post(any(URI.class), anyString()))
                .thenReturn(getResourceStream("twoCreatesErrorResponse.json"));

            accessor.execute(createOperation1, createOperation2);

            try {
                assertThat(createOperation1.get(), is(equalTo("a01i00000000001AAC")));
                fail("Didn't get expected exception");
            } catch (ExecutionException e) {
                assertThat(e.getCause(), is(instanceOf(RecordRequestException.class)));
                assertThat(e.getCause().getMessage(), containsString("INVALID_BATCH_REQUEST"));
            }

            assertThat(createOperation2.get(), is(equalTo("a01i00000000002AAC")));

            verify(connector).post(URI.create("/connect/batch"), getResourceString("twoCreatesRequest.json"));
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
}
