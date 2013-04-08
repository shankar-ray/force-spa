/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

@SalesforceObject(name = "ExplicitName")
public class ExplicitlyNamedBean {

    private String id;

    private String name;

    public String getId() {
        return id;
    }

    @SalesforceField(name = "Id")
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @SalesforceField(name = "Name")
    public void setName(String name) {
        this.name = name;
    }
}
