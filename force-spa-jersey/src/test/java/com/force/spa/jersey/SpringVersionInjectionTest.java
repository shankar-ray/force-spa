/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.ApiVersion;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(locations = {"classpath:com/force/spa/jersey/springVersionInjectionContext.xml"})
public class SpringVersionInjectionTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private SpringRecordAccessorFactory accessorFactory;

    @Test
    public void testVersionInjection() {
        assertThat(accessorFactory, is(not(nullValue())));
        assertThat(accessorFactory.getApiVersion(), is(equalTo(new ApiVersion(27, 0))));
    }
}
