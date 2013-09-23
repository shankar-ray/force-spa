/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.beans;

import org.joda.time.DateTime;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

@SalesforceObject
public class DetailedNamedRecord extends NamedRecord {

    private NamedRecord createdBy;
    private DateTime createdDate;
    private NamedRecord lastModifiedBy;
    private DateTime lastModifiedDate;

    @SalesforceField(name = "CreatedBy")
    public NamedRecord getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(NamedRecord createdBy) {
        this.createdBy = createdBy;
    }

    @SalesforceField(name = "CreatedDate")
    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    @SalesforceField(name = "LastModifiedBy")
    public NamedRecord getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(NamedRecord lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @SalesforceField(name = "LastModifiedDate")
    public DateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(DateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
