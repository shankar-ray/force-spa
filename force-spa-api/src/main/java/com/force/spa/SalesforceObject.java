/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifies a type as representing a Salesforce persistent object.
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface SalesforceObject {

    /**
     * The name of the Salesforce object. Defaults to the Java type name.
     */
    String name() default "";

    /**
     * Whether object processing should leverage server-side metadata.
     */
    boolean metadataAware() default false;
}
