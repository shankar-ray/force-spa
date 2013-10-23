/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

public final class SanitizeUtils {

    private SanitizeUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    /**
     * Sanitize a SOQL string by removing references to customer data so that we can log or display the value.
     */
    public static String sanitizeSoql(String soql) {
        char[] chars = soql.toCharArray();
        StringBuilder builder = new StringBuilder(512);

        char quoteChar = 0;
        int pendingCursor = 0;  // Scanned characters waiting to be transferred to builder
        for (int i = 0, limit = chars.length; i < limit; i++) {
            char c = chars[i];
            if (c == '\\') {
                i++; // Skip over the next character
            } else if (quoteChar != 0) {
                if (c == quoteChar) {
                    quoteChar = 0;  // No longer inside of a quote
                    pendingCursor = i;
                } else {
                    pendingCursor = i + 1;
                }
            } else if (c == '\'' || c == '\"') {
                quoteChar = c;
                builder.append(chars, pendingCursor, (i - pendingCursor + 1));
                builder.append("*****");
            }
        }
        builder.append(chars, pendingCursor, (chars.length - pendingCursor)); // Append the rest of the chars

        return builder.toString();
    }
}
