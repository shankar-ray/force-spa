/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.beans;

import com.force.spa.SalesforceField;

public class Share<T extends Record> extends Record {

    private AccessLevel accessLevel;
    private T parent;
    private RowCause rowCause;
    private String userOrGroupId;

    @SalesforceField(name = "AccessLevel")
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    @SalesforceField(name = "Parent")
    public T getParent() {
        return parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }

    @SalesforceField(name = "RowCause")
    public RowCause getRowCause() {
        return rowCause;
    }

    public void setRowCause(RowCause rowCause) {
        this.rowCause = rowCause;
    }

    @SalesforceField(name = "UserOrGroupId")
    public String getUserOrGroupId() {
        return userOrGroupId;
    }

    public void setUserOrGroupId(String userOrGroupId) {
        this.userOrGroupId = userOrGroupId;
    }

    public enum AccessLevel {
        Read,
        Edit,
        All
    }

    public enum RowCause {
        Owner,
        Manual,
        Rule,
        ImplicitChild,
        ImplicitParent,
        Team,
        Territory,
        TerritoryManual,
        TerritoryRule
    }
}
