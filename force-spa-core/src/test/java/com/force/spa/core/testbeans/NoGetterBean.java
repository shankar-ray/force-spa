/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.Record;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@SalesforceObject
public class NoGetterBean extends Record {

    @SalesforceField(name = "Value1")
    private String value1;

    public void setValue1(String value1) {
        this.value1 = value1;
    }
}
