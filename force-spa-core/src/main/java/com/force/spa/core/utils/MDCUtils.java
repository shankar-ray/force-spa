/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.MDC;

public final class MDCUtils {

    private MDCUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    public static void set(String key, long value) {
        MDC.put(key, Long.toString(value));
    }

    public static void add(String key, long deltaValue) {
        set(key, (NumberUtils.toLong(MDC.get(key), 0) + deltaValue));
    }
}
