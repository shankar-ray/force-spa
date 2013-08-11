/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.Record;

@SalesforceObject
public class EnumBean extends Record {

    @SalesforceField
    private EnumWithAbstractMethod value;

    public EnumWithAbstractMethod getValue() {
        return value;
    }

    public void setValue(EnumWithAbstractMethod value) {
        this.value = value;
    }
}
