/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import static com.force.spa.core.utils.YourKitUtils.clearYourKitData;
import static com.force.spa.core.utils.YourKitUtils.isYourKitPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Test;

import com.force.spa.RecordAccessorConfig;
import com.force.spa.core.rest.AbstractRestRecordAccessorTest;
import com.force.spa.core.testbeans.IndirectToSimpleContainerBean;
import com.force.spa.core.testbeans.PolymorphicFieldBean;
import com.force.spa.core.testbeans.PolymorphicToContainerBean;
import com.force.spa.core.testbeans.RecursiveBean;
import com.force.spa.core.testbeans.SimpleBean;
import com.force.spa.core.testbeans.SimpleContainerBean;

public class SoqlBuilderTest extends AbstractRestRecordAccessorTest {

    private static final int PROFILE_ITERATIONS = 1000;

    private AbstractRecordAccessor accessor;

    @Before
    public void setUp() {
        accessor = new DummyRecordAccessor(new RecordAccessorConfig(), new MappingContext());
    }

    @Test
    public void testBasicWildcard() throws Exception {
        String soqlTemplate = "select * from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(accessor).object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testWildcardWithOnePartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Id,Prefix1.Name,Prefix1.Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(accessor).object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testWildcardWithTwoPartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.Prefix2.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Prefix2.Id,Prefix1.Prefix2.Name,Prefix1.Prefix2.Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(accessor).object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testRelationshipSubquery() throws Exception {
        String soqlTemplate = "select (select RelatedBean.* from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select (select RelatedBean.Id,RelatedBean.Name,RelatedBean.Description from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(accessor).object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testRecursiveTypeReferences() throws Exception {
        String soqlTemplate = "select * from RecursiveBean where Id = '012345678901234'";
        String expectedSoql = "select Id,RecursiveBean.Id,RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.Id from RecursiveBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(accessor).object(RecursiveBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testPolymorphicRelationships() throws Exception {
        String soqlTemplate = "select * from PolymorphicFieldBean where Id = '012345678901234'";
        String expectedSoql = "select Id,TYPEOF Value1 WHEN SimpleBean THEN Id,Name,Description WHEN ExplicitName THEN Id,Name ELSE Id END,TYPEOF Value2 WHEN SimpleBean THEN Id,Name,Description WHEN ExplicitName THEN Id,Name ELSE Id END from PolymorphicFieldBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(accessor).object(PolymorphicFieldBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testSimpleContainerQuery() throws Exception {
        String soqlTemplate = "select * from SimpleContainerBean where Id = '012345678901234'";
        String expectedSoql = "select Id,(SELECT Id,Name,Description from RelatedBeans),(SELECT Id,Name,Description from MoreRelatedBeans) from SimpleContainerBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(accessor).object(SimpleContainerBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testWildcardInQuote() throws Exception {
        String soqlTemplate = "select * from SimpleBean where Name = '*'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Name = '*'";

        String soql = new SoqlBuilder(accessor).object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testEscapedQuoteAndWildcardInQuote() throws Exception {
        String soqlTemplate = "select * from SimpleBean where Name = '\\'*'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Name = '\\'*'";

        String soql = new SoqlBuilder(accessor).object(SimpleBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testIndirectToSimpleContainerQuery() throws Exception {
        String soqlTemplate = "select * from IndirectToSimpleContainerBean where Id = '012345678901234'";

        try {
            new SoqlBuilder(accessor).object(IndirectToSimpleContainerBean.class).template(soqlTemplate).build();
            fail("Didn't get expected exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(equalTo("Beans with parent-to-child fields cannot be referenced indirectly")));
        }
    }

    @Test
    public void testContainerInsidePolymorphism() throws Exception {
        String soqlTemplate = "select * from PolymorphicToContainerBean where Id = '012345678901234'";

        try {
            new SoqlBuilder(accessor).object(PolymorphicToContainerBean.class).template(soqlTemplate).build();
            fail("Didn't get expected exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(equalTo("Beans with parent-to-child fields cannot be referenced polymorphically")));
        }
    }

    @Test
    public void profileSimpleContainerQuery() throws Exception {
        assumeTrue("Profile tests are only run if YourKit is present", isYourKitPresent());

        String soqlTemplate = "select * from SimpleContainerBean where Id = '012345678901234'";
        String expectedSoql = "select Id,(SELECT Id,Name,Description from RelatedBeans),(SELECT Id,Name,Description from MoreRelatedBeans) from SimpleContainerBean where Id = '012345678901234'";

        // Prime caches with a single initial iteration.
        String soql = new SoqlBuilder(accessor).object(SimpleContainerBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));

        clearYourKitData();

        for (int i = 0; i < PROFILE_ITERATIONS; i++) {
            new SoqlBuilder(accessor).object(SimpleContainerBean.class).template(soqlTemplate).build();
        }
    }
}
