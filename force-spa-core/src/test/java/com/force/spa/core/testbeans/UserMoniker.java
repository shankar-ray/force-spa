/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.core.SalesforceField;

public class UserMoniker {

    @SalesforceField(name = "Id")
    private String id;

    @SalesforceField(name = "Name")
    private String name;

    public UserMoniker(String id) {
        this.id = id;
    }

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
}
