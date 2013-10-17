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
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordOperation;
import com.force.spa.RecordRequestException;
import com.force.spa.core.testbeans.SecondarySimpleBean;
import com.force.spa.core.testbeans.SimpleBean;

public class BatchRestRecordAccessorTest extends AbstractRestRecordAccessorTest {

    @Test
    public void testTwoCreatesHasErrorsFirst() throws Throwable {
        try {
            SimpleBean bean1 = new SimpleBean();
            bean1.setName("Name 1");
            bean1.setDescription("Description 1");
            RecordOperation<String> createRecordOperation1 = accessor.newCreateRecordOperation(bean1);

            SimpleBean bean2 = new SimpleBean();
            bean2.setName("Name 2");
            bean2.setDescription("Description 2");
            RecordOperation<String> createRecordOperation2 = accessor.newCreateRecordOperation(bean2);

            when(
                connector.post(any(URI.class), anyString()))
                .thenReturn(getResourceStream("twoCreatesHasErrorsFirstResponse.json"));

            accessor.execute(createRecordOperation1, createRecordOperation2);

            assertThat(createRecordOperation1.get(), is(equalTo("a01i00000000001AAC")));
            assertThat(createRecordOperation2.get(), is(equalTo("a01i00000000002AAC")));

            verify(connector).post(URI.create("/connect/batch"), getResourceString("twoCreatesRequest.json"));
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testTwoCreatesHasErrorsLast() throws Throwable {
        try {
            SimpleBean bean1 = new SimpleBean();
            bean1.setName("Name 1");
            bean1.setDescription("Description 1");
            RecordOperation<String> createRecordOperation1 = accessor.newCreateRecordOperation(bean1);

            SimpleBean bean2 = new SimpleBean();
            bean2.setName("Name 2");
            bean2.setDescription("Description 2");
            RecordOperation<String> createRecordOperation2 = accessor.newCreateRecordOperation(bean2);

            when(
                connector.post(any(URI.class), anyString()))
                .thenReturn(getResourceStream("twoCreatesHasErrorsLastResponse.json"));

            accessor.execute(createRecordOperation1, createRecordOperation2);

            assertThat(createRecordOperation1.get(), is(equalTo("a01i00000000001AAC")));
            assertThat(createRecordOperation2.get(), is(equalTo("a01i00000000002AAC")));

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
            RecordOperation<String> createRecordOperation1 = accessor.newCreateRecordOperation(bean1);

            SimpleBean bean2 = new SimpleBean();
            bean2.setName("Name 2");
            bean2.setDescription("Description 2");
            RecordOperation<String> createRecordOperation2 = accessor.newCreateRecordOperation(bean2);

            when(
                connector.post(any(URI.class), anyString()))
                .thenReturn(getResourceStream("twoCreatesErrorResponse.json"));

            accessor.execute(createRecordOperation1, createRecordOperation2);

            try {
                assertThat(createRecordOperation1.get(), is(equalTo("a01i00000000001AAC")));
                fail("Didn't get expected exception");
            } catch (ExecutionException e) {
                assertThat(e.getCause(), is(instanceOf(RecordRequestException.class)));
                assertThat(e.getCause().getMessage(), containsString("INVALID_BATCH_REQUEST"));
            }

            assertThat(createRecordOperation2.get(), is(equalTo("a01i00000000002AAC")));

            verify(connector).post(URI.create("/connect/batch"), getResourceString("twoCreatesRequest.json"));
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testAmbiguousQueryDeserialization() throws Throwable {
        try {
            when(
                connector.post(any(URI.class), anyString()))
                .thenReturn(getResourceStream("twoSimpleQueriesResponse.json"));

            String soql = "select * from SimpleBean";
            QueryRecordsOperation<SimpleBean> operation1 = accessor.newQueryRecordsOperation(soql, SimpleBean.class);

            QueryRecordsOperation<SecondarySimpleBean> operation2 = accessor.newQueryRecordsOperation(soql, SecondarySimpleBean.class);

            accessor.execute(operation1, operation2);

            List<SimpleBean> beans = operation1.get();

            assertThat(beans.size(), is(equalTo(2)));

            SimpleBean bean1 = beans.get(0);
            assertThat(bean1.getAttributes().get("type"), is(equalTo("SimpleBean")));
            assertThat(bean1.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000001")));
            assertThat(bean1.getId(), is(equalTo("a01i00000000001")));
            assertThat(bean1.getName(), is(equalTo("Name 1")));
            assertThat(bean1.getDescription(), is(equalTo("Description 1")));

            SimpleBean bean2 = beans.get(1);
            assertThat(bean2.getAttributes().get("type"), is(equalTo("SimpleBean")));
            assertThat(bean2.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000002")));
            assertThat(bean2.getId(), is(equalTo("a01i00000000002")));
            assertThat(bean2.getName(), is(equalTo("Name 2")));
            assertThat(bean2.getDescription(), is(equalTo("Description 2")));

        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
}
