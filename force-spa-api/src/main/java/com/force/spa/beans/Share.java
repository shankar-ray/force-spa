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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Share)) return false;
        if (!super.equals(o)) return false;

        Share share = (Share) o;

        if (accessLevel != share.accessLevel) return false;
        if (parent != null ? !parent.equals(share.parent) : share.parent != null) return false;
        if (rowCause != share.rowCause) return false;
        if (userOrGroupId != null ? !userOrGroupId.equals(share.userOrGroupId) : share.userOrGroupId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (accessLevel != null ? accessLevel.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (rowCause != null ? rowCause.hashCode() : 0);
        result = 31 * result + (userOrGroupId != null ? userOrGroupId.hashCode() : 0);
        return result;
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
