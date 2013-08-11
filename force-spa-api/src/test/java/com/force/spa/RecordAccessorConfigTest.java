/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public class RecordAccessorConfigTest {
    @Test
    public void testDefaults() {
        RecordAccessorConfig config = new RecordAccessorConfig();

        assertThat(config.isAuditFieldWritingAllowed(), is(equalTo(false)));
        assertThat(config.isObjectAnnotationRequired(), is(equalTo(false)));
        assertThat(config.isFieldAnnotationRequired(), is(equalTo(false)));
    }

    @Test
    public void testWithAuditFieldWritingAllowed() {
        RecordAccessorConfig config = new RecordAccessorConfig();
        RecordAccessorConfig config2 = config.withAuditFieldWritingAllowed(true);

        assertThat(config, is(not(sameInstance(config2))));
        assertThat(config.isAuditFieldWritingAllowed(), is(equalTo(false)));
        assertThat(config2.isAuditFieldWritingAllowed(), is(equalTo(true)));
    }

    @Test
    public void testWithObjectAnnotationOptional() {
        RecordAccessorConfig config = new RecordAccessorConfig();
        RecordAccessorConfig config2 = config.withObjectAnnotationRequired(true);

        assertThat(config, is(not(sameInstance(config2))));
        assertThat(config.isObjectAnnotationRequired(), is(equalTo(false)));
        assertThat(config2.isObjectAnnotationRequired(), is(equalTo(true)));
    }

    @Test
    public void testWithFieldAnnotationOptional() {
        RecordAccessorConfig config = new RecordAccessorConfig();
        RecordAccessorConfig config2 = config.withFieldAnnotationRequired(true);

        assertThat(config, is(not(sameInstance(config2))));
        assertThat(config.isFieldAnnotationRequired(), is(equalTo(false)));
        assertThat(config2.isFieldAnnotationRequired(), is(equalTo(true)));
    }

    @Test
    public void testEquals() {
        RecordAccessorConfig config1 = new RecordAccessorConfig();
        RecordAccessorConfig config2 = new RecordAccessorConfig();

        assertThat(config1, is(equalTo(config2)));

        assertThat(config1, is(not(equalTo(config1.withAuditFieldWritingAllowed(true)))));
        assertThat(config1, is(not(equalTo(config1.withObjectAnnotationRequired(true)))));
        assertThat(config1, is(not(equalTo(config1.withFieldAnnotationRequired(true)))));

        assertThat(config1.withAuditFieldWritingAllowed(true), is(equalTo(config2.withAuditFieldWritingAllowed(true))));
        assertThat(config1.withObjectAnnotationRequired(true), is(equalTo(config2.withObjectAnnotationRequired(true))));
        assertThat(config1.withFieldAnnotationRequired(true), is(equalTo(config2.withFieldAnnotationRequired(true))));
    }

    @Test
    public void testHashCode() {
        RecordAccessorConfig config1 = new RecordAccessorConfig();
        RecordAccessorConfig config2 = new RecordAccessorConfig();

        assertThat(config1.hashCode(), is(equalTo(config2.hashCode())));

        assertThat(config1.hashCode(), is(not(equalTo(config1.withAuditFieldWritingAllowed(true).hashCode()))));
        assertThat(config1.hashCode(), is(not(equalTo(config1.withObjectAnnotationRequired(true).hashCode()))));
        assertThat(config1.hashCode(), is(not(equalTo(config1.withFieldAnnotationRequired(true).hashCode()))));

        assertThat(config1.withAuditFieldWritingAllowed(true).hashCode(), is(equalTo(config2.withAuditFieldWritingAllowed(true).hashCode())));
        assertThat(config1.withObjectAnnotationRequired(true).hashCode(), is(equalTo(config2.withObjectAnnotationRequired(true).hashCode())));
        assertThat(config1.withFieldAnnotationRequired(true).hashCode(), is(equalTo(config2.withFieldAnnotationRequired(true).hashCode())));
    }
}
