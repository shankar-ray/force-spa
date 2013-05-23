/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.record;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import org.joda.time.DateTime;

@SalesforceObject
public class DetailedNamedRecord extends NamedRecord {
    @SalesforceField(name = "CreatedBy")
    private NamedRecord createdBy;

    @SalesforceField(name = "CreatedDate")
    private DateTime createdDate;

    @SalesforceField(name = "LastModifiedBy")
    private NamedRecord lastModifiedBy;

    @SalesforceField(name = "LastModifiedDate")
    private DateTime lastModifiedDate;

    public NamedRecord getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(NamedRecord createdBy) {
        this.createdBy = createdBy;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public NamedRecord getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(NamedRecord lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public DateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(DateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
