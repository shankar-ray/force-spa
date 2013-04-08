/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.force.spa.core.testbeans.CustomBean;
import com.force.spa.core.testbeans.ExplicitlyNamedBean;
import com.force.spa.core.testbeans.NoGetterBean;
import com.force.spa.core.testbeans.NoSetterBean;
import com.force.spa.core.testbeans.RecursiveBean;
import com.force.spa.core.testbeans.SimpleBean;
import com.force.spa.core.testbeans.SimpleContainerBean;
import com.force.spa.core.testbeans.SimpleEnum;
import com.force.spa.core.testbeans.UnannotatedBean;
import org.junit.Test;

import static com.force.spa.core.HasPropertyName.hasPropertyName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

@SuppressWarnings("unchecked")
public class ObjectMappingContextTest {
    private ObjectMappingContext mappingContext = new ObjectMappingContext();

    @Test
    public void testSimpleBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(SimpleBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("SimpleBean")));
        assertThat(descriptor.hasAttributesMember(), is(true));
        assertThat(descriptor.getAttributesProperty(), is(not(nullValue())));
        assertThat(descriptor.getAttributesProperty(), hasPropertyName("attributes"));
        assertThat(descriptor.hasIdMember(), is(true));
        assertThat(descriptor.getIdProperty(), is(not(nullValue())));
        assertThat(descriptor.getIdProperty(), hasPropertyName("Id"));
        assertThat(descriptor.getRelatedObjects().size(), is(equalTo(0)));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("Name"),
                hasPropertyName("Description"),
                hasPropertyName("attributes")));
    }

    @Test
    public void testSimpleContainerBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(SimpleContainerBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("SimpleContainerBean")));
        assertThat(descriptor.getAttributesProperty(), is(nullValue()));
        assertThat(descriptor.getIdProperty(), is(not(nullValue())));
        assertThat(descriptor.getIdProperty(), hasPropertyName("Id"));
        assertThat(descriptor.getRelatedObjects().size(), is(equalTo(2)));
        assertThat(descriptor.getRelatedObjects(), hasKey("relatedBeans"));
        assertThat(descriptor.getRelatedObjects(), hasKey("moreRelatedBeans"));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("RelatedBeans"),
                hasPropertyName("MoreRelatedBeans")));
    }

    @Test
    public void testCustomBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(CustomBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("namespace__CustomBean__c")));
        assertThat(descriptor.getIdProperty(), is(not(nullValue())));
        assertThat(descriptor.getIdProperty(), hasPropertyName("Id"));
        assertThat(descriptor.getRelatedObjects().size(), is(equalTo(1)));
        assertThat(descriptor.getRelatedObjects(), hasKey("relatedBeans"));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("Name"),
                hasPropertyName("namespace__Value1__c"),
                hasPropertyName("namespace__Value2__c"),
                hasPropertyName("Value3__c"),
                hasPropertyName("RelatedBeans__c")));
    }

    @Test
    public void testNoSetterBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoSetterBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("NoSetterBean")));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("Value1"),
                hasPropertyName("attributes")));
    }

    @Test
    public void testNoGetterBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoGetterBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("NoGetterBean")));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("Value1"),
                hasPropertyName("attributes")));
    }

    @Test
    public void testExplicitlyNamedBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(ExplicitlyNamedBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("ExplicitName")));
    }

    @Test
    public void testUnannotatedBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(UnannotatedBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("UnannotatedBean")));
    }

    @Test
    public void testRecursiveBean() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(RecursiveBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("RecursiveBean")));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(descriptor.getRelatedObjects().size(), is(equalTo(1)));
        assertThat(descriptor.getRelatedObjects().get("recursiveBean"), is(sameInstance(descriptor)));
    }

    @Test
    public void testDescriptorCaching() {
        ObjectDescriptor descriptor1 = mappingContext.getObjectDescriptor(SimpleBean.class);
        ObjectDescriptor descriptor2 = mappingContext.getObjectDescriptor(SimpleBean.class);
        assertThat(descriptor2, is(sameInstance(descriptor1)));
    }

    @Test
    public void testNoDescriptorForEnums() {
        assertThat(mappingContext.getObjectDescriptor(SimpleEnum.class), is(nullValue()));
    }

    @Test
    public void testNoDescriptorForPrimitives() {
        assertThat(mappingContext.getObjectDescriptor(int.class), is(nullValue()));
        assertThat(mappingContext.getObjectDescriptor(long.class), is(nullValue()));
        assertThat(mappingContext.getObjectDescriptor(float.class), is(nullValue()));
        assertThat(mappingContext.getObjectDescriptor(double.class), is(nullValue()));
        assertThat(mappingContext.getObjectDescriptor(boolean.class), is(nullValue()));
    }

    @Test
    public void testNoDescriptorForIntrinsicJavaPackage() {
        assertThat(mappingContext.getObjectDescriptor(Object.class), is(nullValue()));
        assertThat(mappingContext.getObjectDescriptor(String.class), is(nullValue()));
    }

    @Test
    public void testWriteDatesAsTimestampsDisabledByDefault() {
        assertThat(mappingContext.getObjectWriterForCreate().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS), is(false));
        assertThat(mappingContext.getObjectWriterForPatch().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS), is(false));
        assertThat(mappingContext.getObjectWriterForUpdate().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS), is(false));
    }
}
