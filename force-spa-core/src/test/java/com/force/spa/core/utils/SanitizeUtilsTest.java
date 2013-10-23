/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import static com.force.spa.core.utils.SanitizeUtils.sanitizeSoql;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class SanitizeUtilsTest {

    @Test
    public void testSoqlWithNothingToSanitize() {
        String soqlBefore = "select Id,Name from WorkFeedbackRequest";
        String soqlAfter = "select Id,Name from WorkFeedbackRequest";

        assertThat(sanitizeSoql(soqlBefore), is(equalTo(soqlAfter)));
    }

    @Test
    public void testSoqlWithSimpleSanitization() {
        String soqlBefore = "select Id,Name from WorkFeedbackRequest where name = 'President' and manager = 'God'";
        String soqlAfter = "select Id,Name from WorkFeedbackRequest where name = '*****' and manager = '*****'";

        assertThat(sanitizeSoql(soqlBefore), is(equalTo(soqlAfter)));
    }

    @Test
    public void testSoqlWithMissingQuote() {
        String soqlBefore = "select Id,Name from WorkFeedbackRequest where name = 'President' and manager = 'God";
        String soqlAfter = "select Id,Name from WorkFeedbackRequest where name = '*****' and manager = '*****";

        assertThat(sanitizeSoql(soqlBefore), is(equalTo(soqlAfter)));
    }
}
