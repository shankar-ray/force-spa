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

public class RecordAccessorConfigTest {
    @Test
    public void testDefaults() {
        RecordAccessorConfig config = new RecordAccessorConfig();

        assertThat(config.isAuditFieldWritingAllowed(), is(equalTo(false)));
        assertThat(config.isFieldAutodetectEnabled(), is(equalTo(false)));
    }

    @Test
    public void testSettersGetters() {
        RecordAccessorConfig config = new RecordAccessorConfig();

        assertThat(config.isAuditFieldWritingAllowed(), is(equalTo(false)));
        assertThat(config.isFieldAutodetectEnabled(), is(equalTo(false)));

        config.setAuditFieldWritingAllowed(true);
        assertThat(config.isAuditFieldWritingAllowed(), is(equalTo(true)));

        config.setFieldAutodetectEnabled(true);
        assertThat(config.isFieldAutodetectEnabled(), is(equalTo(true)));
    }

    @Test
    public void testEqualsAndHashCode() {
        RecordAccessorConfig config1 = new RecordAccessorConfig();
        RecordAccessorConfig config2 = new RecordAccessorConfig();

        assertThat(config1, is(equalTo(config2)));
        assertThat(config1.hashCode(), is(equalTo(config2.hashCode())));

        config2.setAuditFieldWritingAllowed(true);
        assertThat(config1, is(not(equalTo(config2))));
        assertThat(config1.hashCode(), is(not(equalTo(config2.hashCode()))));
        config2.setAuditFieldWritingAllowed(false);
        assertThat(config1, is(equalTo(config2)));
        assertThat(config1.hashCode(), is(equalTo(config2.hashCode())));

        config2.setFieldAutodetectEnabled(true);
        assertThat(config1, is(not(equalTo(config2))));
        assertThat(config1.hashCode(), is(not(equalTo(config2.hashCode()))));
        config2.setFieldAutodetectEnabled(false);
        assertThat(config1, is(equalTo(config2)));
        assertThat(config1.hashCode(), is(equalTo(config2.hashCode())));
    }
}
