/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.metadata;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("UnusedDeclaration")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectMetadata extends BasicObjectMetadata {

    private List<ChildRelationshipMetadata> childRelationships;
    private List<FieldMetadata> fields;
    private List<RecordTypeMetadata> recordTypeInfos;

    public List<ChildRelationshipMetadata> getChildRelationships() {
        return childRelationships;
    }

    public void setChildRelationships(List<ChildRelationshipMetadata> childRelationships) {
        this.childRelationships = childRelationships;
    }

    public List<FieldMetadata> getFields() {
        return fields;
    }

    public void setFields(List<FieldMetadata> fields) {
        this.fields = fields;
    }

    public List<RecordTypeMetadata> getRecordTypeInfos() {
        return recordTypeInfos;
    }

    public void setRecordTypeInfos(List<RecordTypeMetadata> recordTypeInfos) {
        this.recordTypeInfos = recordTypeInfos;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
