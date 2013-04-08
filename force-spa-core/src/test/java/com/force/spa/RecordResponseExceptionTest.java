/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import org.junit.Test;

import java.io.Serializable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

public class RecordResponseExceptionTest {
    private static final String MESSAGE = "Exception Message";
    private static final String CAUSE_MESSAGE = "Cause Message";

    @Test
    public void testDerivation() {
        assertThat(new RecordResponseException(), isA(RuntimeException.class));
        assertThat(new RecordResponseException(), isA(Serializable.class));
    }

    @Test
    public void testDefaultConstructor() {
        RecordResponseException exception = new RecordResponseException();
        assertThat(exception.getCause(), is(nullValue()));
        assertThat(exception.getMessage(), is(nullValue()));
        assertThat(exception.toString(), is(equalTo(exception.getClass().getName())));
    }

    @Test
    public void testCauseConstructor() {
        RuntimeException cause = new RuntimeException(CAUSE_MESSAGE);

        RecordResponseException exception = new RecordResponseException(cause);
        assertThat(exception.getCause(), is(sameInstance((Throwable) cause)));
        assertThat(exception.getMessage(), is(equalTo(cause.toString())));
        assertThat(exception.toString(), is(equalTo(exception.getClass().getName() + ": " + cause.toString())));
    }

    @Test
    public void testMessageConstructor() {
        RecordResponseException exception = new RecordResponseException(MESSAGE);
        assertThat(exception.getCause(), is(nullValue()));
        assertThat(exception.getMessage(), is(equalTo(MESSAGE)));
        assertThat(exception.toString(), is(equalTo(exception.getClass().getName() + ": " + MESSAGE)));
    }

    @Test
    public void testMessageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException(CAUSE_MESSAGE);

        RecordResponseException exception = new RecordResponseException(MESSAGE, cause);
        assertThat(exception.getCause(), is(sameInstance((Throwable) cause)));
        assertThat(exception.getMessage(), is(equalTo(MESSAGE)));
        assertThat(exception.toString(), is(equalTo(exception.getClass().getName() + ": " + MESSAGE)));
    }
}
