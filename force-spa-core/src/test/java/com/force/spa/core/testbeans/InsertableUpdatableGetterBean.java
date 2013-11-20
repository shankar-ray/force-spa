/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.NamedRecord;

@SalesforceObject
@SuppressWarnings("UnusedDeclaration")
public class InsertableUpdatableGetterBean extends NamedRecord {

    private String notInsertable;

    private String notUpdatable;

    private String notInsertableOrUpdatable;

    @SalesforceField(name = "NotInsertable", insertable = false)
    public String getNotInsertable() {
        return notInsertable;
    }

    public void setNotInsertable(String notInsertable) {
        this.notInsertable = notInsertable;
    }

    @SalesforceField(name = "NotUpdatable", updatable = false)
    public String getNotUpdatable() {
        return notUpdatable;
    }

    public void setNotUpdatable(String notUpdatable) {
        this.notUpdatable = notUpdatable;
    }

    @SalesforceField(name = "NotInsertableOrUpdatable", insertable = false, updatable = false)
    public String getNotInsertableOrUpdatable() {
        return notInsertableOrUpdatable;
    }

    public void setNotInsertableOrUpdatable(String notInsertableOrUpdatable) {
        this.notInsertableOrUpdatable = notInsertableOrUpdatable;
    }
}
