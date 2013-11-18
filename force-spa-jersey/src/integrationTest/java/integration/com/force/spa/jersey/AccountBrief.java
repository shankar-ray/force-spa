/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.NamedRecord;
import com.force.spa.beans.UserBrief;

@SalesforceObject(name = "Account")
@SuppressWarnings("UnusedDeclaration")
public class AccountBrief extends NamedRecord {

    @SalesforceField(name = "AnnualRevenue")
    private Double annualRevenue;

    @SalesforceField(name = "Owner")
    private UserBrief owner;

    @SalesforceField(name = "LastModifiedBy")
    private UserBrief lastModifiedBy;

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

    public UserBrief getOwner() {
        return owner;
    }

    public void setOwner(UserBrief owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountBrief)) return false;
        if (!super.equals(o)) return false;

        AccountBrief that = (AccountBrief) o;

        if (annualRevenue != null ? !annualRevenue.equals(that.annualRevenue) : that.annualRevenue != null)
            return false;
        if (lastModifiedBy != null ? !lastModifiedBy.equals(that.lastModifiedBy) : that.lastModifiedBy != null)
            return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (annualRevenue != null ? annualRevenue.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        return result;
    }
}
