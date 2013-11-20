/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.NamedRecord;
import com.force.spa.beans.Record;

@SuppressWarnings("UnusedDeclaration")
@SalesforceObject
public class PolymorphicFieldWithoutAnnotationBean extends Record {

    @SalesforceField(name = "Value1")
    private Record value1;

    @SalesforceField(name = "Value2")
    private NamedRecord value2;

    public Record getValue1() {
        return value1;
    }

    public void setValue1(Record value1) {
        this.value1 = value1;
    }

    public NamedRecord getValue2() {
        return value2;
    }

    public void setValue2(NamedRecord value2) {
        this.value2 = value2;
    }
}

