/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.junit.Test;

import com.force.spa.CreateRecordOperation;
import com.force.spa.DeleteRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.RecordOperation;
import com.force.spa.UpdateRecordOperation;
import com.force.spa.core.testbeans.PolymorphicFieldBean;
import com.force.spa.core.testbeans.RecursiveBean;
import com.force.spa.core.testbeans.SimpleBean;

public class SoqlBuilderTest extends AbstractRestRecordAccessorTest {

    private AbstractRecordAccessor accessor = new DummyRecordAccessor();

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
        String expectedSoql = "select Id,TYPEOF Value1 WHEN SimpleBean THEN Id,Name,Description WHEN NoAttributesBean THEN Id,Name END,TYPEOF Value2 WHEN SimpleBean THEN Id,Name,Description WHEN NoAttributesBean THEN Id,Name END from PolymorphicFieldBean where Id = '012345678901234'";

        String soql = newSoqlBuilder().object(PolymorphicFieldBean.class).template(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    private SoqlBuilder newSoqlBuilder() {
        return new SoqlBuilder(accessor);
    }

    private static class DummyRecordAccessor extends AbstractRecordAccessor {
        private DummyRecordAccessor() {
            super(new RecordAccessorConfig());
        }

        @Override
        protected void execute(RecordOperation<?> operation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute(List<RecordOperation<?>> operations) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CreateRecordOperation<T> newCreateRecordOperation(T record) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> DeleteRecordOperation<T> newDeleteRecordOperation(String id, Class<T> recordClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> GetRecordOperation<T> newGetRecordOperation(String id, Class<T> recordClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> PatchRecordOperation<T> newPatchRecordOperation(String id, T recordChanges) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> UpdateRecordOperation<T> newUpdateRecordOperation(String id, T record) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soqlTemplate, Class<T> recordClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soqlTemplate, Class<?> recordClass, Class<T> resultClass) {
            throw new UnsupportedOperationException();
        }
    }
}
