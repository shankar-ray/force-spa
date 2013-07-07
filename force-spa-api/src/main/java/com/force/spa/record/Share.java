/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.record;

import com.force.spa.SalesforceField;

public class Share<T extends Record> extends Record {
    @SalesforceField(name = "AccessLevel")
    private AccessLevel accessLevel;

    @SalesforceField(name = "Parent")
    private T parent;

    @SalesforceField(name = "RowCause")
    private RowCause rowCause;

    @SalesforceField(name = "UserOrGroupId")
    private String userOrGroupId;

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public T getParent() {
        return parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }

    public RowCause getRowCause() {
        return rowCause;
    }

    public void setRowCause(RowCause rowCause) {
        this.rowCause = rowCause;
    }

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
