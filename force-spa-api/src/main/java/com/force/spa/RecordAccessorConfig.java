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
public class RecordAccessorConfig implements Serializable {
    private static final long serialVersionUID = -697566942312359870L;

    private boolean auditFieldWritingAllowed;
    private boolean fieldAutodetectEnabled;

    public RecordAccessorConfig() {
        auditFieldWritingAllowed = false;
        fieldAutodetectEnabled = false;
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
     * Sets whether audit fields such as "CreatedBy", "CreatedDate", "LastModifiedBy", and "LastModifiedDate" are sent
     * to the server. These are special protected fields normally controlled by the server and can not be written by the
     * application. If, however, a special org perm called "CreateAuditFields" is enabled then the application is
     * allowed to set the fields. If the org perm is enabled you you need to write to the fields then you can set this
     * option to true to allow the values to pass through the record accessor to the server.
     * <p/>
     * Default is "false".
     *
     * @param auditFieldWritingAllowed an indication of whether audit fields should be sent to the server
     */
    public void setAuditFieldWritingAllowed(boolean auditFieldWritingAllowed) {
        this.auditFieldWritingAllowed = auditFieldWritingAllowed;
    }

    /**
     * Indicates whether unannotated Javabean fields are automatically detected and consider to be salesforce fields.
     * <p/>
     * Default is "false"
     *
     * @return an indication of whether unannotated fields are automatically detected
     */
    public boolean isFieldAutodetectEnabled() {
        return fieldAutodetectEnabled;
    }

    /**
     * Sets whether unannotated Javabean fields are automatically detected and consider to be salesforce fields.
     * <p/>
     * Default is "false"
     *
     * @param fieldAutodetectEnabled an indication of whether unannotated fields should be automatically detected
     */
    public void setFieldAutodetectEnabled(boolean fieldAutodetectEnabled) {
        this.fieldAutodetectEnabled = fieldAutodetectEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecordAccessorConfig that = (RecordAccessorConfig) o;
        return new EqualsBuilder()
            .append(this.auditFieldWritingAllowed, that.auditFieldWritingAllowed)
            .append(this.fieldAutodetectEnabled, that.fieldAutodetectEnabled)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(auditFieldWritingAllowed)
            .append(fieldAutodetectEnabled)
            .toHashCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this);
    }
}
