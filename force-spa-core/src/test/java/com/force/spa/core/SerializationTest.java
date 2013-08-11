/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.force.spa.core.testbeans.EnumBean;
import com.force.spa.core.testbeans.EnumWithAbstractMethod;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class SerializationTest {

    private final ObjectMappingContext mappingContext = new ObjectMappingContext();

    @Test
    public void testEnumBeanSerialization() throws Exception {
        EnumBean bean = new EnumBean();
        bean.setValue(EnumWithAbstractMethod.ONE);
        String serializedBean = mappingContext.getObjectWriterForCreate().writeValueAsString(bean);
        assertThat(serializedBean, is(equalTo("{\"value\":\"ONE\"}")));
    }

    @Test
    public void testEnumBeanDeserialization() throws Exception {
        String serializedBean = "{\"value\":\"TWO\"}";
        ObjectReader objectReader = mappingContext.getObjectReader();
        JsonNode jsonNode = objectReader.readTree(serializedBean);
        JsonParser jsonParser = objectReader.treeAsTokens(jsonNode);
        EnumBean bean = objectReader.readValue(jsonParser, EnumBean.class);

        assertThat(bean, is(notNullValue()));
        assertThat(bean.getValue(), is(equalTo(EnumWithAbstractMethod.TWO)));
    }
}
