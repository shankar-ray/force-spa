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

    /**
     * Whether this is the primary bean for a Salesforce object when there are multiple beans defined for the same
     * object. This comes into play during polymorphic parsing when there is no other hint to help choose the right
     * bean.
     */
    boolean primary() default false;
}
