/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.Record;

/**
 * A bean that has a field with no Jackson accessor.
 */
@SuppressWarnings("UnusedDeclaration")
@SalesforceObject
public class FieldWithNoAccessorBean extends Record {

    private transient SimpleBean hiddenValue1;

    @SalesforceField(name = "Value1")
    public void setValue1(SimpleBean value1) {
        this.hiddenValue1 = value1;
    }
}

