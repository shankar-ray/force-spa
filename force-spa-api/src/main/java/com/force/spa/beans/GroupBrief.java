/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.beans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

@SalesforceObject(name = "Group")
public class GroupBrief extends NamedRecord {

    private String developerName;

    private String type;

    @SalesforceField(name = "DeveloperName")
    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    @SalesforceField(name = "Type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupBrief)) return false;
        if (!super.equals(o)) return false;

        GroupBrief that = (GroupBrief) o;

        if (developerName != null ? !developerName.equals(that.developerName) : that.developerName != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (developerName != null ? developerName.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}

