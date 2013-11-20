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
public class InsertableUpdatableGetterOverrideBean extends NamedRecord {

    private String notInsertable;

    private String notUpdatable;

    private String notInsertableOrUpdatable;

    @SalesforceField(name = "NotInsertableOverriddenToBeWrong", insertable = true)
    public String getNotInsertable() {
        return notInsertable;
    }

    public void setNotInsertable(String notInsertable) {
        this.notInsertable = notInsertable;
    }

    @SalesforceField(name = "NotUpdatableOverriddenToBeWrong", updatable = true)
    public String getNotUpdatable() {
        return notUpdatable;
    }

    public void setNotUpdatable(String notUpdatable) {
        this.notUpdatable = notUpdatable;
    }

    @SalesforceField(name = "NotInsertableOrUpdatableOverriddenToBeWrong", insertable = true, updatable = true)
    public String getNotInsertableOrUpdatable() {
        return notInsertableOrUpdatable;
    }

    public void setNotInsertableOrUpdatable(String notInsertableOrUpdatable) {
        this.notInsertableOrUpdatable = notInsertableOrUpdatable;
    }
}
