/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.beans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

@SalesforceObject
public class NamedRecord extends Record {

    private String name;

    public static NamedRecord withId(String id) {
        NamedRecord record = new NamedRecord();
        record.setId(id);
        return record;
    }

    public static NamedRecord withName(String name) {
        NamedRecord record = new NamedRecord();
        record.setName(name);
        return record;
    }

    @SalesforceField(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
