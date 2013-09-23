/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.beans;

import java.util.Map;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

@SalesforceObject
public class Record {

    private String id;
    private Map<String, String> attributes;

    public static Record withId(String id) {
        Record record = new Record();
        record.setId(id);
        return record;
    }

    @SalesforceField(name = "Id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SalesforceField(name = "attributes")
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
