/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import java.util.List;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.NamedRecord;
import com.force.spa.beans.UserBrief;

@SalesforceObject
public class Account extends NamedRecord {

    @SalesforceField(name = "AnnualRevenue")
    private Double annualRevenue;

    @SalesforceField(name = "Owner")
    private UserBrief owner;

    @SalesforceField(name = "LastModifiedBy")
    private UserBrief lastModifiedBy;

    @SalesforceField(name = "Notes")
    private List<Note> notes;

    public Double getAnnualRevenue() {
        return annualRevenue;
    }

    public void setAnnualRevenue(Double annualRevenue) {
        this.annualRevenue = annualRevenue;
    }

    public UserBrief getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(UserBrief lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public UserBrief getOwner() {
        return owner;
    }

    public void setOwner(UserBrief owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Account account = (Account) o;

        if (annualRevenue != null ? !annualRevenue.equals(account.annualRevenue) : account.annualRevenue != null)
            return false;
        if (lastModifiedBy != null ? !lastModifiedBy.equals(account.lastModifiedBy) : account.lastModifiedBy != null)
            return false;
        if (notes != null ? !notes.equals(account.notes) : account.notes != null) return false;
        if (owner != null ? !owner.equals(account.owner) : account.owner != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (annualRevenue != null ? annualRevenue.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        result = 31 * result + (notes != null ? notes.hashCode() : 0);
        return result;
    }
}
