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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetailedNamedRecord)) return false;
        if (!super.equals(o)) return false;

        DetailedNamedRecord that = (DetailedNamedRecord) o;

        if (createdBy != null ? !createdBy.equals(that.createdBy) : that.createdBy != null) return false;
        if (createdDate != null ? !createdDate.equals(that.createdDate) : that.createdDate != null) return false;
        if (lastModifiedBy != null ? !lastModifiedBy.equals(that.lastModifiedBy) : that.lastModifiedBy != null)
            return false;
        if (lastModifiedDate != null ? !lastModifiedDate.equals(that.lastModifiedDate) : that.lastModifiedDate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        result = 31 * result + (lastModifiedDate != null ? lastModifiedDate.hashCode() : 0);
        return result;
    }
}
