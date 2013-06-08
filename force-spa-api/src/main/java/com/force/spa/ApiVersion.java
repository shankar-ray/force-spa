/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.io.Serializable;

public class ApiVersion implements Comparable<ApiVersion>, Serializable {

    private final Integer majorVersion;
    private final Integer minorVersion;
    private volatile String cachedToString;

    public ApiVersion(Integer majorVersion, Integer minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;

        this.cachedToString = majorVersion + "." + minorVersion;
    }

    public ApiVersion(String value) {
        if (value == null)
            throw new IllegalArgumentException("value is null");

        String[] parts = value.split("\\.");
        if (parts.length != 2)
            throw new IllegalArgumentException("incorrect format for API version");

        this.majorVersion = Integer.valueOf(parts[0]);
        this.minorVersion = Integer.valueOf(parts[1]);

        this.cachedToString = majorVersion + "." + minorVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiVersion that = (ApiVersion) o;

        if (!majorVersion.equals(that.majorVersion)) return false;
        if (!minorVersion.equals(that.minorVersion)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = majorVersion.hashCode();
        result = 31 * result + minorVersion.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return cachedToString;
    }

    @Override
    public int compareTo(ApiVersion that) {
        int majorResult = majorVersion.compareTo(that.majorVersion);
        if (majorResult != 0)
            return majorResult;

        return minorVersion.compareTo(that.minorVersion);
    }
}
