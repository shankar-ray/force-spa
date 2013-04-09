/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.core.SalesforceField;
import com.force.spa.core.SalesforceObject;

/**
 * A test bean with fields that choose whether they are insertable or updatable.
 */
@SalesforceObject
public class InsertableUpdatableBean {

    @SalesforceField(name = "Id")
    private String id;

    @SalesforceField(name = "Name")
    private String name;

    @SalesforceField(name = "NotInsertable", insertable = false)
    private String notInsertable;

    @SalesforceField(name = "NotUpdatable", updatable = false)
    private String notUpdatable;

    @SalesforceField(name = "NotInsertableOrUpdatable", insertable = false, updatable = false)
    private String notInsertableOrUpdatable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
