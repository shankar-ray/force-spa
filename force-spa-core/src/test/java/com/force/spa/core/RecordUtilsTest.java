/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.force.spa.core.testbeans.NoGetterBean;
import com.force.spa.core.testbeans.NoIdBean;
import com.force.spa.core.testbeans.NoSetterBean;
import com.force.spa.core.testbeans.SimpleBean;
import com.force.spa.core.testbeans.UnannotatedBean;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

public class RecordUtilsTest {
    private ObjectMappingContext mappingContext = new ObjectMappingContext();

    @Test
    public void testGetAttributesSimple() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(SimpleBean.class);
        SimpleBean instance = new SimpleBean();
        Map<String, String> attributes = new HashMap<String, String>();
        instance.setAttributes(attributes);

        assertThat(RecordUtils.getAttributes(descriptor, instance), is(sameInstance(attributes)));
        assertThat(RecordUtils.getAttributes(descriptor.getAttributesProperty(), instance), is(sameInstance(attributes)));
    }

    @Test
    public void testGetAttributesNoSetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoSetterBean.class);
        NoSetterBean instance = new NoSetterBean();

        assertThat(RecordUtils.getAttributes(descriptor, instance), is(nullValue()));
        assertThat(RecordUtils.getAttributes(descriptor.getAttributesProperty(), instance), is(nullValue()));
    }

    @Test
    public void testGetAttributesNoGetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoGetterBean.class);
        NoGetterBean instance = new NoGetterBean();
        Map<String, String> attributes = new HashMap<String, String>();
        instance.setAttributes(attributes);

        assertThat(RecordUtils.getAttributes(descriptor, instance), is(sameInstance(attributes)));
        assertThat(RecordUtils.getAttributes(descriptor.getAttributesProperty(), instance), is(sameInstance(attributes)));
    }

    @Test
    public void testGetAttributesWhenNone() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(UnannotatedBean.class);
        UnannotatedBean instance = new UnannotatedBean();

        try {
            RecordUtils.getAttributes(descriptor, instance);
            fail("Didn't get expected exception");
        } catch (IllegalArgumentException e) {
            // Exception expected because no attribute property exists.
        }
    }

    @Test
    public void testGetIdSimple() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(SimpleBean.class);
        SimpleBean instance = new SimpleBean();
        String id = "012345678901234";
        instance.setId(id);

        assertThat(RecordUtils.getId(descriptor, instance), is(sameInstance(id)));
        assertThat(RecordUtils.getId(descriptor.getIdProperty(), instance), is(sameInstance(id)));
    }

    @Test
    public void testGetIdNoSetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoSetterBean.class);
        NoSetterBean instance = new NoSetterBean();

        assertThat(RecordUtils.getId(descriptor, instance), is(nullValue()));
        assertThat(RecordUtils.getId(descriptor.getIdProperty(), instance), is(nullValue()));
    }

    @Test
    public void testGetIdNoGetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoGetterBean.class);
        NoGetterBean instance = new NoGetterBean();
        String id = "012345678901234";
        instance.setId(id);

        assertThat(RecordUtils.getId(descriptor, instance), is(sameInstance(id)));
        assertThat(RecordUtils.getId(descriptor.getIdProperty(), instance), is(sameInstance(id)));
    }

    @Test
    public void testGetIdWhenNone() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoIdBean.class);
        UnannotatedBean instance = new UnannotatedBean();

        try {
            RecordUtils.getId(descriptor, instance);
            fail("Didn't get expected exception");
        } catch (IllegalArgumentException e) {
            // Exception expected because no id property exists.
        }
    }

    @Test
    public void testSetIdSimple() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(SimpleBean.class);
        SimpleBean instance = new SimpleBean();
        String id = "012345678901234";

        RecordUtils.setId(descriptor, instance, id);
        assertThat(instance.getId(), is(sameInstance(id)));
    }

    @Test
    public void testSetIdNoSetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoSetterBean.class);
        NoSetterBean instance = new NoSetterBean();
        String id = "012345678901234";

        RecordUtils.setId(descriptor, instance, id);
        assertThat(instance.getId(), is(sameInstance(id)));
    }

    @Test
    public void testSetIdNoGetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoGetterBean.class);
        NoGetterBean instance = new NoGetterBean();
        String id = "012345678901234";

        RecordUtils.setId(descriptor, instance, id);
        assertThat(instance.id, is(sameInstance(id)));
    }

    @Test
    public void testSetIdWhenNone() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoIdBean.class);
        NoIdBean instance = new NoIdBean();
        String id = "012345678901234";

        try {
            RecordUtils.setId(descriptor, instance, id);
            fail("Didn't get expected exception");
        } catch (IllegalArgumentException e) {
            // Exception expected because no id property exists.
        }
    }

}
