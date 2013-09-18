/*
 * Copyright, 2012, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.force.spa.RecordQuery;
import com.force.spa.RecordQueryResult;
import com.force.spa.RecordRequestException;
import com.force.spa.RecordResponseException;
import com.force.spa.core.testbeans.DateTimeBean;
import com.force.spa.core.testbeans.InsertableUpdatableBean;
import com.force.spa.core.testbeans.NoAttributesBean;
import com.force.spa.core.testbeans.PolymorphicFieldBean;
import com.force.spa.core.testbeans.SimpleBean;
import com.force.spa.core.testbeans.SimpleContainerBean;
import com.force.spa.core.testbeans.StandardFieldBean;
import com.force.spa.record.NamedRecord;

public class RestRecordAccessorTest extends AbstractRestRecordAccessorTest {

    @Test
    public void testSimpleCreate() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("Name 1");
        bean.setDescription("Description 1");

        when(
            mockConnector.post(any(URI.class), anyString()))
            .thenReturn(getResourceStream("createSuccessResponse.json"));

        String id = accessor.create(bean);
        assertThat(id, is(equalTo("a01i00000000001AAC")));

        verify(mockConnector).post(URI.create("/sobjects/SimpleBean"), getResourceString("simpleCreateRequest.json"));
    }

    @Test
    public void testCreateErrorResponse() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("Name 1");
        bean.setDescription("Description 1");

        when(
            mockConnector.post(any(URI.class), anyString()))
            .thenReturn(getResourceStream("createErrorResponse.json"));

        try {
            accessor.create(bean);
            fail("Didn't get expected exception");
        } catch (RecordResponseException e) {
            assertThat(e.getMessage(), is(equalTo("Error message 1; Error message 2")));
        }
    }

    @Test
    public void testCreateInvalidResponse() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("Name 1");
        bean.setDescription("Description 1");

        when(
            mockConnector.post(any(URI.class), anyString()))
            .thenReturn(getResourceStream("createInvalidResponse.json"));

        try {
            accessor.create(bean);
            fail("Didn't get expected exception");
        } catch (RecordResponseException e) {
            assertThat(e.getMessage(), is(equalTo("JSON response is missing expected fields")));
        }
    }

    @Test
    public void testStandardFieldCreate() throws Exception {
        StandardFieldBean bean = new StandardFieldBean();
        bean.setName("Name 1");
        bean.setCreatedBy(NamedRecord.withId("a01i00000000201"));
        bean.setCreatedDate(new DateTime());
        bean.setLastModifiedBy(NamedRecord.withId("a01i00000000202"));
        bean.setLastModifiedDate(new DateTime());
        bean.setOwner(NamedRecord.withId("a01i00000000203"));

        when(
            mockConnector.post(any(URI.class), anyString()))
            .thenReturn(getResourceStream("createSuccessResponse.json"));

        String id = accessor.create(bean);
        assertThat(id, is(equalTo("a01i00000000001AAC")));

        verify(mockConnector).post(
            URI.create("/sobjects/StandardFieldBean"), getResourceString("standardFieldCreateRequest.json"));
    }

    @Test
    public void testCreateInsertableUpdatable() throws Exception {
        InsertableUpdatableBean bean = new InsertableUpdatableBean();
        bean.setName("Name 1");
        bean.setNotInsertable("Updatable but not insertable value");
        bean.setNotUpdatable("Insertable but not updatable value");
        bean.setNotInsertableOrUpdatable("Not insertable or updatable value");

        when(
            mockConnector.post(any(URI.class), anyString()))
            .thenReturn(getResourceStream("createSuccessResponse.json"));

        String id = accessor.create(bean);
        assertThat(id, is(equalTo("a01i00000000001AAC")));

        verify(mockConnector).post(
            URI.create("/sobjects/InsertableUpdatableBean"), getResourceString("createInsertableUpdatableRequest.json"));
    }

    @Test
    public void testSimplePatch() throws Exception {
        SimpleBean beanChanges = new SimpleBean();
        beanChanges.setDescription("Description 1");

        doNothing().when(mockConnector).patch(any(URI.class), anyString());
        accessor.patch("a01i00000000001AAC", beanChanges);

        verify(mockConnector).patch(
            URI.create("/sobjects/SimpleBean/a01i00000000001AAC"), getResourceString("simplePatchRequest.json"));
    }

    @Test
    public void testSimpleUpdate() throws Exception {
        SimpleBean beanChanges = new SimpleBean();
        beanChanges.setName("Name 2");

        doNothing().when(mockConnector).patch(any(URI.class), anyString());
        accessor.update("a01i00000000001AAC", beanChanges);

        verify(mockConnector).patch(
            URI.create("/sobjects/SimpleBean/a01i00000000001AAC"), getResourceString("simpleUpdateRequest.json"));

        SimpleBean beanChanges2 = new SimpleBean();
        beanChanges2.setDescription("Description 1");

        doNothing().when(mockConnector).patch(any(URI.class), anyString());
        accessor.patch("a01i00000000001AAC", beanChanges2);

        verify(mockConnector).patch(
            URI.create("/sobjects/SimpleBean/a01i00000000001AAC"), getResourceString("simplePatchRequest.json"));
    }

    @Test
    public void testUpdateByBeanButNoId() throws Exception {
        SimpleBean beanChanges = new SimpleBean();
        beanChanges.setDescription("Description 1");

        doNothing().when(mockConnector).patch(any(URI.class), anyString());
        try {
            accessor.update(beanChanges);
            fail("Didn't get expected exception");
        } catch (RecordRequestException e) {
            assertThat(e.getMessage(), is(equalTo("Record bean does not have an id value set")));
        }
    }

    @Test
    public void testStandardFieldPatch() throws Exception {
        StandardFieldBean beanChanges = new StandardFieldBean();
        beanChanges.setName("Name 1");
        beanChanges.setCreatedBy(NamedRecord.withId("a01i00000000201"));
        beanChanges.setCreatedDate(new DateTime());
        beanChanges.setLastModifiedBy(NamedRecord.withId("a01i00000000202"));
        beanChanges.setLastModifiedDate(new DateTime());
        beanChanges.setOwner(NamedRecord.withId("a01i00000000203"));

        doNothing().when(mockConnector).patch(any(URI.class), anyString());
        accessor.patch("a01i00000000001AAC", beanChanges);

        verify(mockConnector).patch(
            URI.create("/sobjects/StandardFieldBean/a01i00000000001AAC"), getResourceString("standardFieldPatchRequest.json"));
    }

    @Test
    public void testDeleteByBean() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setId("a01i00000000001AAC");

        doNothing().when(mockConnector).delete(any(URI.class));
        accessor.delete(bean);

        verify(mockConnector).delete(URI.create("/sobjects/SimpleBean/a01i00000000001AAC"));
    }

    @Test
    public void testSimpleDelete() throws Exception {
        doNothing().when(mockConnector).delete(any(URI.class));
        accessor.delete("a01i00000000001AAC", SimpleBean.class);

        verify(mockConnector).delete(URI.create("/sobjects/SimpleBean/a01i00000000001AAC"));
    }

    @Test
    public void testDeleteByBeanButNoId() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setDescription("Description 1");

        doNothing().when(mockConnector).delete(any(URI.class));

        try {
            accessor.delete(bean);
            fail("Didn't get expected exception");
        } catch (RecordRequestException e) {
            assertThat(e.getMessage(), is(equalTo("Record bean does not have an id value set")));
        }
    }

    @Test
    public void testSimpleGet() throws Exception {
        when(mockConnector.get(any(URI.class))).thenReturn(getResourceStream("simpleGetResponse.json"));

        SimpleBean bean = accessor.get("a01i00000000001AAC", SimpleBean.class);

        assertThat(bean, is(not(nullValue())));

        assertThat(bean.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000001")));
        assertThat(bean.getId(), is(equalTo("a01i00000000001")));
        assertThat(bean.getName(), is(equalTo("Name 1")));
        assertThat(bean.getDescription(), is(equalTo("Description 1")));
    }

    @Test
    public void testSimpleQuery() throws Exception {
        when(mockConnector.get(any(URI.class))).thenReturn(getResourceStream("simpleQueryResponse.json"));

        RecordQuery<SimpleBean> query = accessor.createQuery("select * from SimpleBean", SimpleBean.class);
        List<SimpleBean> beans = query.execute();

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
    }

    @Test
    public void testSimpleQueryThroughResult() throws Exception {
        when(mockConnector.get(any(URI.class))).thenReturn(getResourceStream("simpleQueryResponse.json"));

        RecordQuery<SimpleBean> query = accessor.createQuery("select * from SimpleBean", SimpleBean.class);
        RecordQueryResult<SimpleBean> result = query.executeForResult();

        assertThat(result.getTotalSize(), is(equalTo(2)));

        List<SimpleBean> beans = result.getRecords();
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
    }

    @Test
    public void testSubquery() throws Exception {
        when(mockConnector.get(any(URI.class))).thenReturn(getResourceStream("simpleSubqueryResponse.json"));

        List<SimpleContainerBean> containerBeans =
            accessor.createQuery("select * from SimpleContainerBean", SimpleContainerBean.class).execute();

        assertThat(containerBeans.size(), is(equalTo(1)));

        SimpleContainerBean containerBean1 = containerBeans.get(0);
        assertThat(containerBean1.getRelatedBeans().size(), is(equalTo(2)));

        SimpleBean bean1 = containerBean1.getRelatedBeans().get(0);
        assertThat(bean1.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean1.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000001")));
        assertThat(bean1.getId(), is(equalTo("a01i00000000001")));
        assertThat(bean1.getName(), is(equalTo("Name 1")));
        assertThat(bean1.getDescription(), is(equalTo("Description 1")));

        SimpleBean bean2 = containerBean1.getRelatedBeans().get(1);
        assertThat(bean2.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean2.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000002")));
        assertThat(bean2.getId(), is(equalTo("a01i00000000002")));
        assertThat(bean2.getName(), is(equalTo("Name 2")));
        assertThat(bean2.getDescription(), is(equalTo("Description 2")));

        assertThat(containerBean1.getMoreRelatedBeans().length, is(equalTo(2)));

        SimpleBean bean3 = containerBean1.getMoreRelatedBeans()[0];
        assertThat(bean3.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean3.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000003")));
        assertThat(bean3.getId(), is(equalTo("a01i00000000003")));
        assertThat(bean3.getName(), is(equalTo("Name 3")));
        assertThat(bean3.getDescription(), is(equalTo("Description 3")));

        SimpleBean bean4 = containerBean1.getMoreRelatedBeans()[1];
        assertThat(bean4.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean4.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000004")));
        assertThat(bean4.getId(), is(equalTo("a01i00000000004")));
        assertThat(bean4.getName(), is(equalTo("Name 4")));
        assertThat(bean4.getDescription(), is(equalTo("Description 4")));
    }

    @Test
    public void testCountToQueryResult() throws Exception {
        when(mockConnector.get(any(URI.class))).thenReturn(getResourceStream("countQueryResponse.json"));

        RecordQuery<SimpleBean> query = accessor.createQuery("select count(Id) FROM SimpleBean GROUP BY Name", SimpleBean.class);
        RecordQueryResult<SimpleBean> result = query.executeForResult();

        assertThat(result, is(notNullValue()));
        assertThat(result.getTotalSize(), is(equalTo(2)));
        assertThat(result.isDone(), is(true));
    }

    @Test
    public void testAggregateQueryToJsonNode() throws Exception {
        when(mockConnector.get(any(URI.class))).thenReturn(getResourceStream("aggregateQueryResponse.json"));

        RecordQuery<SimpleBean> query = accessor.createQuery("select count(Id),Name FROM SimpleBean GROUP BY Name", SimpleBean.class);
        List<JsonNode> jsonNodes = query.execute(JsonNode.class);

        assertThat(jsonNodes.size(), is(equalTo(2)));

        JsonNode node1 = jsonNodes.get(0);
        assertThat(node1.get("expr0").asInt(), is(equalTo(1)));
        assertThat(node1.get("Name").asText(), is(equalTo("Name 1")));

        JsonNode node2 = jsonNodes.get(1);
        assertThat(node2.get("expr0").asInt(), is(equalTo(1)));
        assertThat(node2.get("Name").asText(), is(equalTo("Name 2")));
    }

    @Test
    public void testAggregateQueryToJsonNodeThroughResult() throws Exception {
        when(mockConnector.get(any(URI.class))).thenReturn(getResourceStream("aggregateQueryResponse.json"));

        RecordQuery<SimpleBean> query = accessor.createQuery("select count(Id),Name FROM SimpleBean GROUP BY Name", SimpleBean.class);
        RecordQueryResult<JsonNode> result = query.executeForResult(JsonNode.class);

        assertThat(result.getTotalSize(), is(equalTo(2)));

        List<JsonNode> jsonNodes = result.getRecords();
        assertThat(jsonNodes.size(), is(equalTo(2)));

        JsonNode node1 = jsonNodes.get(0);
        assertThat(node1.get("expr0").asInt(), is(equalTo(1)));
        assertThat(node1.get("Name").asText(), is(equalTo("Name 1")));

        JsonNode node2 = jsonNodes.get(1);
        assertThat(node2.get("expr0").asInt(), is(equalTo(1)));
        assertThat(node2.get("Name").asText(), is(equalTo("Name 2")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAggregateQueryToMap() throws Exception {
        when(mockConnector.get(any(URI.class))).thenReturn(getResourceStream("aggregateQueryResponse.json"));

        RecordQuery<SimpleBean> query = accessor.createQuery("select count(Id),Name FROM SimpleBean GROUP BY Name", SimpleBean.class);
        List<Map> maps = query.execute(Map.class);

        assertThat(maps.size(), is(equalTo(2)));

        Map<String, Object> map1 = maps.get(0);
        assertThat((Integer) map1.get("expr0"), is(equalTo(1)));
        assertThat((String) map1.get("Name"), is(equalTo("Name 1")));

        Map node2 = maps.get(1);
        assertThat((Integer) node2.get("expr0"), is(equalTo(1)));
        assertThat((String) node2.get("Name"), is(equalTo("Name 2")));
    }

    @Test
    public void testDateTimePatch() throws Exception {
        TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        iso8601Format.setTimeZone(gmtTimeZone);
        SimpleDateFormat justDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        justDateFormat.setTimeZone(gmtTimeZone);

        DateTimeBean beanChanges = new DateTimeBean();
        beanChanges.setJavaDateAndTime(iso8601Format.parse("1999-04-01T08:14:56.000+0000"));
        beanChanges.setJavaDateOnly(justDateFormat.parse("1999-04-01"));
        beanChanges.setJodaDateAndTime(new DateTime(beanChanges.getJavaDateAndTime().getTime(), DateTimeZone.UTC));
        beanChanges.setJodaDateOnly(LocalDate.parse("1999-04-01"));

        doNothing().when(mockConnector).patch(any(URI.class), anyString());
        accessor.patch("a01i00000000001AAC", beanChanges);

        verify(mockConnector).patch(
            URI.create("/sobjects/DateTimeBean/a01i00000000001AAC"), getResourceString("dateTimePatchRequest.json"));
    }

    @Test
    public void testDateTimeQuery() throws Exception {
        TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        iso8601Format.setTimeZone(gmtTimeZone);
        SimpleDateFormat justDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        justDateFormat.setTimeZone(gmtTimeZone);

        Date javaDateAndTime = iso8601Format.parse("1999-04-01T08:14:56.000+0000");
        Date javaDateOnly = justDateFormat.parse("1999-04-01");
        DateTime jodaDateAndTime = new DateTime(javaDateAndTime.getTime(), DateTimeZone.UTC);
        LocalDate jodaDateOnly = LocalDate.parse("1999-04-01");

        when(mockConnector.get(any(URI.class))).thenReturn(getResourceStream("dateTimeQueryResponse.json"));

        DateTimeBean bean = accessor.get("a01i00000000001AAC", DateTimeBean.class);

        assertThat(bean, is(not(nullValue())));

        assertThat(bean.getJavaDateAndTime(), is(equalTo(javaDateAndTime)));
        assertThat(bean.getJavaDateOnly(), is(equalTo(javaDateOnly)));
        assertThat(bean.getJodaDateAndTime(), is(equalTo(jodaDateAndTime)));
        assertThat(bean.getJodaDateOnly(), is(equalTo(jodaDateOnly)));
    }

    @Test
    public void testPolymorphicIdCreate() throws Exception {
        PolymorphicFieldBean polymorphicFieldBean = new PolymorphicFieldBean();
        SimpleBean simpleBean = new SimpleBean();
        simpleBean.setId("a01i00000000202");
        polymorphicFieldBean.setValue1(simpleBean);
        NoAttributesBean noAttributesBean = new NoAttributesBean();
        noAttributesBean.setId("a01i00000000303");
        polymorphicFieldBean.setValue2(noAttributesBean);

        when(
            mockConnector.post(any(URI.class), anyString()))
            .thenReturn(getResourceStream("createSuccessResponse.json"));

        String id = accessor.create(polymorphicFieldBean);
        assertThat(id, is(equalTo("a01i00000000001AAC")));

        verify(mockConnector).post(
            URI.create("/sobjects/PolymorphicFieldBean"), getResourceString("polymorphicIdCreateRequest.json"));
    }

    @Test
    public void testPolymorphicKeyLookupCreate() throws Exception {
        PolymorphicFieldBean polymorphicFieldBean = new PolymorphicFieldBean();
        SimpleBean simpleBean = new SimpleBean();
        simpleBean.setName("Name 1");
        polymorphicFieldBean.setValue1(simpleBean);
        NoAttributesBean noAttributesBean = new NoAttributesBean();
        noAttributesBean.setName("Name 2");
        polymorphicFieldBean.setValue2(noAttributesBean);

        when(
            mockConnector.post(any(URI.class), anyString()))
            .thenReturn(getResourceStream("createSuccessResponse.json"));

        String id = accessor.create(polymorphicFieldBean);
        assertThat(id, is(equalTo("a01i00000000001AAC")));

        verify(mockConnector).post(
            URI.create("/sobjects/PolymorphicFieldBean"), getResourceString("polymorphicKeyLookupCreateRequest.json"));
    }
}
