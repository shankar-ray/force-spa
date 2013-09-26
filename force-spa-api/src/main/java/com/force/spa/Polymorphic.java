/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a member (field or setter method) as being a polymorphic field.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Polymorphic {

    /**
     * A list of possible field types for a polymorphic field.
     */
    Class<?>[] value();
}
