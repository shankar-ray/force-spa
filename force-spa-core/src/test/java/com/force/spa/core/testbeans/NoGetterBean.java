/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

import java.util.Map;

@SalesforceObject
public class NoGetterBean {

    @SalesforceField(name = "Id")
    public String id;

    @SalesforceField(name = "Value1")
    private String value1;

    private Map<String, String> attributes;

    public void setId(String id) {
        this.id = id;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
