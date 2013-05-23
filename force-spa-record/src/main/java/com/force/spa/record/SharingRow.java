/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.record;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

// TODO See if this works. Add enum too?
@SalesforceObject
public class SharingRow extends Record {
    @SalesforceField(name = "AccessLevel")
    private String accessLevel;

    @SalesforceField(name = "Parent")
    private Record parent;

    @SalesforceField(name = "RowCause")
    private String rowCause;

    @SalesforceField(name = "UserOrGroup")
    private NamedRecord userOrGroup;

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Record getParent() {
        return parent;
    }

    public void setParent(Record parent) {
        this.parent = parent;
    }

    public String getRowCause() {
        return rowCause;
    }

    public void setRowCause(String rowCause) {
        this.rowCause = rowCause;
    }

    public NamedRecord getUserOrGroup() {
        return userOrGroup;
    }

    public void setUserOrGroup(NamedRecord userOrGroup) {
        this.userOrGroup = userOrGroup;
    }
}
