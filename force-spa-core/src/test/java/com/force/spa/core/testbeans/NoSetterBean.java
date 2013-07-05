/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

import java.util.Map;

@SuppressWarnings("ALL")
@SalesforceObject
public class NoSetterBean {

    @SalesforceField(name = "Id")
    private String id;

    @SalesforceField(name = "Value1")
    private String value1;

    @SalesforceField(name = "attributes")
    private Map<String, String> attributes;

    public String getId() {
        return id;
    }

    public String getValue1() {
        return value1;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
