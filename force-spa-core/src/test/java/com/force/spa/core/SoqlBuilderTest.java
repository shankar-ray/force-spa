/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.rest.AbstractRestRecordAccessorTest;
import com.force.spa.core.testbeans.PolymorphicFieldBean;
import com.force.spa.core.testbeans.RecursiveBean;
import com.force.spa.core.testbeans.SimpleBean;

public class SoqlBuilderTest extends AbstractRestRecordAccessorTest {

    private AbstractRecordAccessor accessor;

    @Before
    public void setUp() {
        accessor = new DummyRecordAccessor(new RecordAccessorConfig(), new MappingContext());
    }

    @Test
    public void testBasicWildcard() throws Exception {
        String soqlTemplate = "select * from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = newSoqlBuilder().object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testWildcardWithOnePartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Id,Prefix1.Name,Prefix1.Description from SimpleBean where Id = '012345678901234'";

        String soql = newSoqlBuilder().object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testWildcardWithTwoPartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.Prefix2.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Prefix2.Id,Prefix1.Prefix2.Name,Prefix1.Prefix2.Description from SimpleBean where Id = '012345678901234'";

        String soql = newSoqlBuilder().object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testRelationshipSubquery() throws Exception {
        String soqlTemplate = "select (select RelatedBean.* from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select (select RelatedBean.Id,RelatedBean.Name,RelatedBean.Description from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";

        String soql = newSoqlBuilder().object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testRecursiveTypeReferences() throws Exception {
        String soqlTemplate = "select * from RecursiveBean where Id = '012345678901234'";
        String expectedSoql = "select Id,RecursiveBean.Id,RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.Id from RecursiveBean where Id = '012345678901234'";

        String soql = newSoqlBuilder().object(RecursiveBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testPolymorphicRelationships() throws Exception {
        String soqlTemplate = "select * from PolymorphicFieldBean where Id = '012345678901234'";
        String expectedSoql = "select Id,TYPEOF Value1 WHEN SimpleBean THEN Id,Name,Description WHEN ExplicitName THEN Id,Name ELSE Id END,TYPEOF Value2 WHEN SimpleBean THEN Id,Name,Description WHEN ExplicitName THEN Id,Name ELSE Id END from PolymorphicFieldBean where Id = '012345678901234'";

        String soql = newSoqlBuilder().object(PolymorphicFieldBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    private SoqlBuilder newSoqlBuilder() {
        return new SoqlBuilder(accessor);
    }
}
