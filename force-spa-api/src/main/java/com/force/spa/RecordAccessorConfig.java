/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;

/**
 * Tunable configuration for {@link RecordAccessor} instances.
 */
public final class RecordAccessorConfig implements Serializable {
    private static final long serialVersionUID = -697566942312359870L;

    private final boolean auditFieldWritingAllowed;
    private final boolean objectAnnotationRequired;
    private final boolean fieldAnnotationRequired;

    public RecordAccessorConfig() {
        this(false, false, false);
    }

    private RecordAccessorConfig(boolean auditFieldWritingAllowed, boolean objectAnnotationRequired, boolean fieldAnnotationRequired) {
        this.auditFieldWritingAllowed = auditFieldWritingAllowed;
        this.objectAnnotationRequired = objectAnnotationRequired;
        this.fieldAnnotationRequired = fieldAnnotationRequired;
    }

    /**
     * Indicates whether audit fields such as "CreatedBy", "CreatedDate", "LastModifiedBy", and "LastModifiedDate" are
     * sent to the server. These are special protected fields normally controlled by the server and can not be written
     * by the application. If, however, a special org perm called "CreateAuditFields" is enabled then the application is
     * allowed to set the fields. If the org perm is enabled you you need to write to the fields then you can set this
     * option to true to allow the values to pass through the record accessor to the server.
     * <p/>
     * Default is "false".
     *
     * @return an indication of whether audit fields are sent to the server
     */
    public boolean isAuditFieldWritingAllowed() {
        return auditFieldWritingAllowed;
    }

    /**
     * Indicates whether the {@link SalesforceObject} annotation is required. If <code>true</code> then all Javabeans
     * intended for use with Salesforce objects use must be annotated. If <code>false</code>, unannotated Javabeans are
     * automatically detected and consider to be Salesforce objects.
     * <p/>
     * Default is "false"
     *
     * @return an indication of whether the {@link SalesforceObject} annotation is required
     */
    public boolean isObjectAnnotationRequired() {
        return objectAnnotationRequired;
    }

    /**
     * Indicates whether the {@link SalesforceField} annotation is required. If <code>true</code> then all Javabean
     * fields intended to map to Salesforce fields must be annotated. If <code>false</code>, unannotated Javabean fields
     * are automatically detected and consider to be Salesforce fields.
     * <p/>
     * Default is "false"
     *
     * @return an indication of whether the {@link SalesforceField} annotation is required
     */
    public boolean isFieldAnnotationRequired() {
        return fieldAnnotationRequired;
    }

    /**
     * Returns a new instance with the specified setting for whether audit fields should be sent to the server.
     *
     * @param auditFieldWritingAllowed an indication of whether audit fields should be sent to the server
     * @see #isAuditFieldWritingAllowed()
     */
    public RecordAccessorConfig withAuditFieldWritingAllowed(boolean auditFieldWritingAllowed) {
        return new RecordAccessorConfig(auditFieldWritingAllowed, objectAnnotationRequired, fieldAnnotationRequired);
    }

    /**
     * Returns a new instance with the specified setting for whether the {@link SalesforceObject} annotation is
     * required.
     *
     * @param objectAnnotationRequired an indication of whether the {@link SalesforceObject} annotation is required
     * @see #isObjectAnnotationRequired()
     */
    public RecordAccessorConfig withObjectAnnotationRequired(boolean objectAnnotationRequired) {
        return new RecordAccessorConfig(auditFieldWritingAllowed, objectAnnotationRequired, fieldAnnotationRequired);
    }

    /**
     * Returns a new instance with the specified setting for whether the {@link SalesforceField} annotation is
     * required.
     *
     * @param fieldAnnotationRequired an indication of whether the {@link SalesforceField} annotation is required
     * @see #isFieldAnnotationRequired()
     */
    public RecordAccessorConfig withFieldAnnotationRequired(boolean fieldAnnotationRequired) {
        return new RecordAccessorConfig(auditFieldWritingAllowed, objectAnnotationRequired, fieldAnnotationRequired);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecordAccessorConfig that = (RecordAccessorConfig) o;
        return new EqualsBuilder()
            .append(this.auditFieldWritingAllowed, that.auditFieldWritingAllowed)
            .append(this.objectAnnotationRequired, that.objectAnnotationRequired)
            .append(this.fieldAnnotationRequired, that.fieldAnnotationRequired)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(auditFieldWritingAllowed)
            .append(objectAnnotationRequired)
            .append(fieldAnnotationRequired)
            .toHashCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this);
    }
}
