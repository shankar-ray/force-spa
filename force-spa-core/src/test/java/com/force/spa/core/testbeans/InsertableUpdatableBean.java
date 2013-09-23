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
public class InsertableUpdatableBean extends NamedRecord {

    @SalesforceField(name = "NotInsertable", insertable = false)
    private String notInsertable;

    @SalesforceField(name = "NotUpdatable", updatable = false)
    private String notUpdatable;

    @SalesforceField(name = "NotInsertableOrUpdatable", insertable = false, updatable = false)
    private String notInsertableOrUpdatable;

    public String getNotInsertable() {
        return notInsertable;
    }

    public void setNotInsertable(String notInsertable) {
        this.notInsertable = notInsertable;
    }

    public String getNotInsertableOrUpdatable() {
        return notInsertableOrUpdatable;
    }

    public void setNotInsertableOrUpdatable(String notInsertableOrUpdatable) {
        this.notInsertableOrUpdatable = notInsertableOrUpdatable;
    }

    public String getNotUpdatable() {
        return notUpdatable;
    }

    public void setNotUpdatable(String notUpdatable) {
        this.notUpdatable = notUpdatable;
    }
}
