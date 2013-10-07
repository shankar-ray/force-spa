/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.Polymorphic;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.Record;

@SuppressWarnings("UnusedDeclaration")
@SalesforceObject
public class PolymorphicFieldBean extends Record {

    @Polymorphic({SimpleBean.class, ExplicitlyNamedBean.class})
    @SalesforceField(name = "Value1")
    private Record value1;

    @Polymorphic({SimpleBean.class, ExplicitlyNamedBean.class})
    @SalesforceField(name = "Value2")
    private Record value2;

    public Object getValue1() {
        return value1;
    }

    public void setValue1(Record value1) {
        this.value1 = value1;
    }

    public Record getValue2() {
        return value2;
    }

    public void setValue2(Record value2) {
        this.value2 = value2;
    }
}

