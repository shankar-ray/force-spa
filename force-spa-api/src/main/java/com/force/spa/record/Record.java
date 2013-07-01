/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.record;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

import java.util.Map;

@SalesforceObject
public class Record {
    // The Salesforce server wants this to be the first field in the serialization if it is present.
    @SalesforceField(name = "attributes")
    private Map<String, String> attributes;

    @SalesforceField(name = "Id")
    private String id;

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
