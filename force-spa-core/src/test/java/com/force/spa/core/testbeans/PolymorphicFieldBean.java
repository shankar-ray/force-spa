/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.Polymorphic;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.Record;

@SuppressWarnings("UnusedDeclaration")
@SalesforceObject
public class PolymorphicFieldBean extends Record {

    @Polymorphic({SimpleBean.class, NoAttributesBean.class})
    @SalesforceField(name = "Value1")
    private Object value1;

    @Polymorphic({SimpleBean.class, NoAttributesBean.class})
    @SalesforceField(name = "Value2")
    private Object value2;

    public Object getValue1() {
        return value1;
    }

    public void setValue1(Object value1) {
        this.value1 = value1;
    }

    public Object getValue2() {
        return value2;
    }

    public void setValue2(Object value2) {
        this.value2 = value2;
    }
}

