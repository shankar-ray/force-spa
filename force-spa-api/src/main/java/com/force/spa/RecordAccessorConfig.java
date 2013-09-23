/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Tunable configuration for {@link RecordAccessor} instances.
 */
public class RecordAccessorConfig implements Serializable {

    public static final RecordAccessorConfig DEFAULT = new RecordAccessorConfig();

    private static final long serialVersionUID = -697566942312359870L;

    private final AuthorizationConnector authorizationConnector;
    private final ApiVersion apiVersion;
    private final boolean auditFieldWritingAllowed;
    private final boolean objectAnnotationRequired;
    private final boolean fieldAnnotationRequired;

    public RecordAccessorConfig() {
        this(null, null, false, false, false);
    }

    protected RecordAccessorConfig(
        AuthorizationConnector authorizationConnector, ApiVersion apiVersion,
        boolean auditFieldWritingAllowed, boolean objectAnnotationRequired, boolean fieldAnnotationRequired) {

        this.authorizationConnector = authorizationConnector;
        this.apiVersion = apiVersion;
        this.auditFieldWritingAllowed = auditFieldWritingAllowed;
        this.objectAnnotationRequired = objectAnnotationRequired;
        this.fieldAnnotationRequired = fieldAnnotationRequired;
    }

    /**
     * Returns the {@link AuthorizationConnector} to use for {@link RecordAccessor} or {@link MetadataAccessor} accessor
     * instances created with this configuration.
     *
     * @return the authorization connector or <code>null</code> if not specified
     */
    public final AuthorizationConnector getAuthorizationConnector() {
        return authorizationConnector;
    }

    /**
     * Returns the {@link ApiVersion} to use for {@link RecordAccessor} or {@link MetadataAccessor} accessor instances
     * created with this configuration.
     * <p/>
     * If not specified, the most recent api version supported by the server is used.
     *
     * @return the api version or <code>null</code> if not specified
     */
    public final ApiVersion getApiVersion() {
        return apiVersion;
    }

    /**
     * Indicates whether audit fields such as "CreatedBy", "CreatedDate", "LastModifiedBy", and "LastModifiedDate" are
     * sent to the server. These are special protected fields normally controlled by the server and can not be written
     * by the application. If, however, a special org perm called "CreateAuditFields" is enabled then the application is
     * allowed to set the fields. If the org perm is enabled you you need to write to the fields then you can set this
     * option to true to allow the values to pass through the record accessor to the server.
     * <p/>
     * The default is "false".
     *
     * @return an indication of whether audit fields are sent to the server
     */
    public final boolean isAuditFieldWritingAllowed() {
        return auditFieldWritingAllowed;
    }

    /**
     * Indicates whether the {@link SalesforceObject} annotation is required. If <code>true</code> then all Javabeans
     * intended for use with Salesforce objects use must be annotated. If <code>false</code>, unannotated Javabeans are
     * automatically detected and consider to be Salesforce objects.
     * <p/>
     * The default is "false"
     *
     * @return an indication of whether the {@link SalesforceObject} annotation is required
     */
    public final boolean isObjectAnnotationRequired() {
        return objectAnnotationRequired;
    }

    /**
     * Indicates whether the {@link SalesforceField} annotation is required. If <code>true</code> then all Javabean
     * fields intended to map to Salesforce fields must be annotated. If <code>false</code>, unannotated Javabean fields
     * are automatically detected and consider to be Salesforce fields.
     * <p/>
     * The default is "false"
     *
     * @return an indication of whether the {@link SalesforceField} annotation is required
     */
    public final boolean isFieldAnnotationRequired() {
        return fieldAnnotationRequired;
    }

    /**
     * Returns a new instance with the specified setting for the {@link AuthorizationConnector} to use for {@link
     * RecordAccessor} instances created with this configuration.
     *
     * @param authorizationConnector the authorization connector
     * @see #getAuthorizationConnector()
     */
    public RecordAccessorConfig withAuthorizationConnector(AuthorizationConnector authorizationConnector) {
        return new RecordAccessorConfig(
            authorizationConnector, getApiVersion(),
            auditFieldWritingAllowed, objectAnnotationRequired, fieldAnnotationRequired);
    }

    /**
     * Returns a new instance with the specified setting for the {@link ApiVersion} to use with {@link RecordAccessor}
     * instances created with this configuration.
     *
     * @param apiVersion the api version
     * @see #getApiVersion()
     */
    public RecordAccessorConfig withApiVersion(ApiVersion apiVersion) {
        return new RecordAccessorConfig(
            getAuthorizationConnector(), apiVersion,
            auditFieldWritingAllowed, objectAnnotationRequired, fieldAnnotationRequired);
    }

    /**
     * Returns a new instance with the specified setting for whether audit fields should be sent to the server.
     *
     * @param auditFieldWritingAllowed an indication of whether audit fields should be sent to the server
     * @see #isAuditFieldWritingAllowed()
     */
    public RecordAccessorConfig withAuditFieldWritingAllowed(boolean auditFieldWritingAllowed) {
        return new RecordAccessorConfig(
            getAuthorizationConnector(), getApiVersion(),
            auditFieldWritingAllowed, objectAnnotationRequired, fieldAnnotationRequired);
    }

    /**
     * Returns a new instance with the specified setting for whether the {@link SalesforceObject} annotation is
     * required.
     *
     * @param objectAnnotationRequired an indication of whether the {@link SalesforceObject} annotation is required
     * @see #isObjectAnnotationRequired()
     */
    public RecordAccessorConfig withObjectAnnotationRequired(boolean objectAnnotationRequired) {
        return new RecordAccessorConfig(
            getAuthorizationConnector(), getApiVersion(),
            auditFieldWritingAllowed, objectAnnotationRequired, fieldAnnotationRequired);
    }

    /**
     * Returns a new instance with the specified setting for whether the {@link SalesforceField} annotation is
     * required.
     *
     * @param fieldAnnotationRequired an indication of whether the {@link SalesforceField} annotation is required
     * @see #isFieldAnnotationRequired()
     */
    public RecordAccessorConfig withFieldAnnotationRequired(boolean fieldAnnotationRequired) {
        return new RecordAccessorConfig(
            getAuthorizationConnector(), getApiVersion(),
            auditFieldWritingAllowed, objectAnnotationRequired, fieldAnnotationRequired);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecordAccessorConfig that = (RecordAccessorConfig) o;
        return new EqualsBuilder()
            .append(this.authorizationConnector, that.authorizationConnector)
            .append(this.apiVersion, that.apiVersion)
            .append(this.auditFieldWritingAllowed, that.auditFieldWritingAllowed)
            .append(this.objectAnnotationRequired, that.objectAnnotationRequired)
            .append(this.fieldAnnotationRequired, that.fieldAnnotationRequired)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(authorizationConnector)
            .append(apiVersion)
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
