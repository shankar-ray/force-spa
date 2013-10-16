/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import static com.force.spa.core.utils.HasFieldName.hasFieldName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.force.spa.core.testbeans.CustomBean;
import com.force.spa.core.testbeans.EnumBean;
import com.force.spa.core.testbeans.EnumWithAbstractMethod;
import com.force.spa.core.testbeans.ExplicitlyNamedBean;
import com.force.spa.core.testbeans.FieldWithNoAccessorBean;
import com.force.spa.core.testbeans.NoGetterBean;
import com.force.spa.core.testbeans.NoSetterBean;
import com.force.spa.core.testbeans.RecursiveBean;
import com.force.spa.core.testbeans.SimpleBean;
import com.force.spa.core.testbeans.SimpleContainerBean;
import com.force.spa.core.testbeans.TransientFieldBean;
import com.force.spa.core.testbeans.UnannotatedBean;

@SuppressWarnings("unchecked")
public class MappingContextTest {
    private final MappingContext mappingContext = new MappingContext();

    @Test
    public void testSimpleBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(SimpleBean.class);
        assertThat(descriptor.getName(), is(equalTo("SimpleBean")));
        assertThat(descriptor.getAttributesField(), is(not(nullValue())));
        assertThat(descriptor.getAttributesField(), hasFieldName("attributes"));
        assertThat(descriptor.getIdField(), is(not(nullValue())));
        assertThat(descriptor.getIdField(), hasFieldName("Id"));
        assertThat(descriptor.getFields(), is(not(nullValue())));
        assertThat(
            descriptor.getFields(),
            containsInAnyOrder(
                hasFieldName("Id"),
                hasFieldName("Name"),
                hasFieldName("Description"),
                hasFieldName("attributes")));
    }

    @Test
    public void testSimpleContainerBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(SimpleContainerBean.class);
        assertThat(descriptor.getName(), is(equalTo("SimpleContainerBean")));
        assertThat(descriptor.hasAttributesField(), is(true));
        assertThat(descriptor.getIdField(), is(not(nullValue())));
        assertThat(descriptor.getIdField(), hasFieldName("Id"));
        assertThat(descriptor.getField("RelatedBeans").isRelationship(), is(true));
        assertThat(descriptor.getField("MoreRelatedBeans").isRelationship(), is(true));
        assertThat(descriptor.getFields(), is(not(nullValue())));
        assertThat(
            descriptor.getFields(),
            containsInAnyOrder(
                hasFieldName("Id"),
                hasFieldName("attributes"),
                hasFieldName("RelatedBeans"),
                hasFieldName("MoreRelatedBeans")));
    }

    @Test
    public void testCustomBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(CustomBean.class);
        assertThat(descriptor.getName(), is(equalTo("namespace__CustomBean__c")));
        assertThat(descriptor.getIdField(), is(not(nullValue())));
        assertThat(descriptor.getIdField(), hasFieldName("Id"));
        assertThat(descriptor.getField("RelatedBeans__r").isRelationship(), is(true));
        assertThat(descriptor.getFields(), is(not(nullValue())));
        assertThat(
            descriptor.getFields(),
            containsInAnyOrder(
                hasFieldName("Id"),
                hasFieldName("attributes"),
                hasFieldName("Name"),
                hasFieldName("namespace__Value1__c"),
                hasFieldName("namespace__Value2__c"),
                hasFieldName("Value3__c"),
                hasFieldName("RelatedBeans__r")));
    }

    @Test
    public void testNoSetterBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoSetterBean.class);
        assertThat(descriptor.getName(), is(equalTo("NoSetterBean")));
        assertThat(descriptor.getFields(), is(not(nullValue())));
        assertThat(
            descriptor.getFields(),
            containsInAnyOrder(
                hasFieldName("Id"),
                hasFieldName("Value1"),
                hasFieldName("attributes")));
    }

    @Test
    public void testNoGetterBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoGetterBean.class);
        assertThat(descriptor.getName(), is(equalTo("NoGetterBean")));
        assertThat(descriptor.getFields(), is(not(nullValue())));
        assertThat(
            descriptor.getFields(),
            containsInAnyOrder(
                hasFieldName("Id"),
                hasFieldName("attributes"),
                hasFieldName("Value1")));
    }

    @Test
    public void testExplicitlyNamedBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(ExplicitlyNamedBean.class);
        assertThat(descriptor.getName(), is(equalTo("ExplicitName")));
    }

    @Test
    public void testUnannotatedBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(UnannotatedBean.class);
        assertThat(descriptor.getName(), is(equalTo("UnannotatedBean")));
    }

    @Test
    public void testRecursiveBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(RecursiveBean.class);
        assertThat(descriptor.getName(), is(equalTo("RecursiveBean")));
        assertThat(descriptor.getFields(), is(not(nullValue())));
        assertThat(descriptor.getField("RecursiveBean").isRelationship(), is(true));
        assertThat(descriptor.getField("RecursiveBean").getRelatedObject(), is(sameInstance(descriptor)));
    }

    @Test
    public void testDescriptorCaching() {
        ObjectDescriptor descriptor1 = mappingContext.getObjectDescriptor(SimpleBean.class);
        ObjectDescriptor descriptor2 = mappingContext.getObjectDescriptor(SimpleBean.class);
        assertThat(descriptor2, is(sameInstance(descriptor1)));
    }

    @Test
    public void testNoDescriptorForEnums() {
        assertThat(mappingContext.findObjectDescriptor(EnumWithAbstractMethod.class), is(nullValue()));
    }

    @Test
    public void testNoDescriptorForPrimitives() {
        assertThat(mappingContext.findObjectDescriptor(int.class), is(nullValue()));
        assertThat(mappingContext.findObjectDescriptor(long.class), is(nullValue()));
        assertThat(mappingContext.findObjectDescriptor(float.class), is(nullValue()));
        assertThat(mappingContext.findObjectDescriptor(double.class), is(nullValue()));
        assertThat(mappingContext.findObjectDescriptor(boolean.class), is(nullValue()));
    }

    @Test
    public void testNoDescriptorForIntrinsicJavaPackage() {
        assertThat(mappingContext.findObjectDescriptor(Object.class), is(nullValue()));
        assertThat(mappingContext.findObjectDescriptor(String.class), is(nullValue()));
    }

    @Test
    public void testWriteDatesAsTimestampsDisabledByDefault() {
        assertThat(mappingContext.getObjectWriterForCreate().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS), is(false));
        assertThat(mappingContext.getObjectWriterForPatch().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS), is(false));
        assertThat(mappingContext.getObjectWriterForUpdate().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS), is(false));
    }

    @Test
    public void testEnumBean() throws Exception {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(EnumBean.class);
        assertThat(descriptor.getName(), is(equalTo("EnumBean")));
        assertThat(
            descriptor.getFields(),
            containsInAnyOrder(
                hasFieldName("Id"),
                hasFieldName("attributes"),
                hasFieldName("value")));
    }

    @Test
    public void testTransientFieldBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(TransientFieldBean.class);
        assertThat(descriptor.getName(), is(equalTo("TransientFieldBean")));
        assertThat(descriptor.getAttributesField(), is(not(nullValue())));
        assertThat(descriptor.getAttributesField(), hasFieldName("attributes"));
        assertThat(descriptor.getIdField(), is(not(nullValue())));
        assertThat(descriptor.getIdField(), hasFieldName("Id"));
        assertThat(descriptor.getFields(), is(not(nullValue())));
        assertThat(
            descriptor.getFields(),
            containsInAnyOrder(
                hasFieldName("Id"),
                hasFieldName("attributes")));
    }

    @Test
    public void testFieldWithNoAccessorBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(FieldWithNoAccessorBean.class);
        assertThat(descriptor.getName(), is(equalTo("FieldWithNoAccessorBean")));
        assertThat(descriptor.getFields(), is(not(nullValue())));
        assertThat(descriptor.getFields(), containsInAnyOrder(
            hasFieldName("Value1"),
            hasFieldName("Id"),
            hasFieldName("attributes")));
    }
}
