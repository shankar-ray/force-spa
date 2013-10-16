/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.force.spa.core.FieldDescriptor;

/**
 * Does the {@link com.force.spa.core.FieldDescriptor} have the specified name?
 */
public class HasFieldName<T extends FieldDescriptor> extends TypeSafeMatcher<T> {
    private final String expectedName;

    HasFieldName(String expectedName) {
        this.expectedName = expectedName;
    }

    @Override
    protected boolean matchesSafely(T item) {
        return item.getName().equals(expectedName);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expectedName);
    }

    @Factory
    public static <T extends FieldDescriptor> Matcher<T> hasFieldName(String name) {
        return new HasFieldName<>(name);
    }
}
