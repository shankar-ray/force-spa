/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.Record;

@SuppressWarnings("UnusedDeclaration")
@SalesforceObject
public class NoSetterBean extends Record {

    @SalesforceField(name = "Value1")
    private String value1;

    public String getValue1() {
        return value1;
    }
}
