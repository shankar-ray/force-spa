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
 * Marks a member (field or setter method) as representing a Salesforce persistent field.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface SalesforceField {

    /**
     * The name of the Salesforce field. Defaults to the Java property or field name.
     */
    String name() default "";

    /**
     * Whether the member's value should be persisted during "create".
     */
    boolean insertable() default true;

    /**
     * Whether the member's value should be persisted during "update" or "patch".
     */
    boolean updatable() default true;
}
