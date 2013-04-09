/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a member (field or setter method) as representing a Salesforce parent-to-child relationship.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ParentToChild {
}
