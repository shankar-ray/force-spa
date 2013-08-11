/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.force.spa.core.testbeans.NoAttributesBean;
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
    private final ObjectMappingContext mappingContext = new ObjectMappingContext();

    @Test
    public void testGetAttributesSimple() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(SimpleBean.class);
        SimpleBean instance = new SimpleBean();
        Map<String, String> attributes = new HashMap<String, String>();
        instance.setAttributes(attributes);

        assertThat(RecordUtils.getAttributes(descriptor, instance), is(sameInstance(attributes)));
    }

    @Test
    public void testGetAttributesNoSetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoSetterBean.class);
        NoSetterBean instance = new NoSetterBean();

        assertThat(RecordUtils.getAttributes(descriptor, instance), is(nullValue()));
    }

    @Test
    public void testGetAttributesNoGetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoGetterBean.class);
        NoGetterBean instance = new NoGetterBean();
        Map<String, String> attributes = new HashMap<String, String>();
        instance.setAttributes(attributes);

        assertThat(RecordUtils.getAttributes(descriptor, instance), is(sameInstance(attributes)));
    }

    @Test
    public void testGetAttributesWhenNone() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoAttributesBean.class);
        NoAttributesBean instance = new NoAttributesBean();

        try {
            RecordUtils.getAttributes(descriptor, instance);
            fail("Didn't get expected exception");
        } catch (IllegalStateException e) {
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
    }

    @Test
    public void testGetIdNoSetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoSetterBean.class);
        NoSetterBean instance = new NoSetterBean();

        assertThat(RecordUtils.getId(descriptor, instance), is(nullValue()));
    }

    @Test
    public void testGetIdNoGetter() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoGetterBean.class);
        NoGetterBean instance = new NoGetterBean();
        String id = "012345678901234";
        instance.setId(id);

        assertThat(RecordUtils.getId(descriptor, instance), is(sameInstance(id)));
    }

    @Test
    public void testGetIdWhenNone() {
        ObjectDescriptor descriptor = mappingContext.getObjectDescriptor(NoIdBean.class);
        UnannotatedBean instance = new UnannotatedBean();

        try {
            RecordUtils.getId(descriptor, instance);
            fail("Didn't get expected exception");
        } catch (IllegalStateException e) {
            // Exception expected because no id property exists.
        }
    }
}
