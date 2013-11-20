/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.force.spa.SalesforceObject;

/**
 * Miscellaneous utilities for asking questions about objects and their metadata.
 */
public final class IntrospectionUtils {
    // The names of standard Salesforce properties.
    private static final Set<String> STANDARD_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "Id", "Name", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate", "Owner",
            "MasterLabel", "DeveloperName", "Language", "RecordType", "attributes")));

    /**
     * The names of standard Salesforce properties that can not be sent with record creation.
     */
    private static final Set<String> NON_INSERTABLE_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "Id", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate")));

    /**
     * The names of standard Salesforce properties that can not be sent with record creation when "CreateAuditFields"
     * per is enabled in the org.
     */
    private static final Set<String> NON_INSERTABLE_PROPERTIES_AUDIT_OK = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "Id")));

    /**
     * The names of standard Salesforce properties that can not be sent with record update.
     */
    private static final Set<String> NON_UPDATABLE_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "Id", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate")));

    /**
     * The names of standard Salesforce properties that can not be sent with record update when "CreateAuditFields" per
     * is enabled in the org.
     */
    private static final Set<String> NON_UPDATABLE_PROPERTIES_AUDIT_OK = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "Id")));

    private IntrospectionUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isStandardProperty(String name) {
        return STANDARD_PROPERTIES.contains(name);
    }

    static boolean isNonInsertableStandardProperty(String name, boolean auditFieldWritingAllowed) {
        if (auditFieldWritingAllowed) {
            return NON_INSERTABLE_PROPERTIES_AUDIT_OK.contains(name);
        } else {
            return NON_INSERTABLE_PROPERTIES.contains(name);
        }
    }

    static boolean isNonUpdatableStandardProperty(String name, boolean auditFieldWritingAllowed) {
        if (auditFieldWritingAllowed) {
            return NON_UPDATABLE_PROPERTIES_AUDIT_OK.contains(name);
        } else {
            return NON_UPDATABLE_PROPERTIES.contains(name);
        }
    }

    static boolean canBeSalesforceObject(Class<?> type, boolean objectAnnotationRequired) {
        if (type.isPrimitive() || isIntrinsicJavaPackage(type.getPackage()) || isJodaTimePackage(type.getPackage())) {
            return false;
        }

        if (isEnum(type)) {
            return false;
        }

        return hasSalesforceObjectAnnotation(type) || !objectAnnotationRequired;
    }

    private static boolean hasSalesforceObjectAnnotation(Class<?> type) {
        return type.getAnnotation(SalesforceObject.class) != null;
    }

    private static boolean isIntrinsicJavaPackage(Package aPackage) {
        return (aPackage != null) && (aPackage.getName().startsWith("java."));
    }

    private static boolean isJodaTimePackage(Package aPackage) {
        return (aPackage != null) && (aPackage.getName().startsWith("org.joda.time"));
    }

    /*
     * An enhanced check for "isEnum" because the standard Class.isEnum() isn't always enough. When an enum includes
     * abstract methods an inner anonymous class arises and even though that class has the enum modifier bit set,
     * Class.isEnum() returns false which is not the answer we need.
     */
    private static boolean isEnum(Class<?> type) {
        return ((type.getModifiers() & 0x4000)) != 0;
    }
}
