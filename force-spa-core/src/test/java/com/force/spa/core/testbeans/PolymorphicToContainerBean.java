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
public class PolymorphicToContainerBean extends Record {

    @Polymorphic({SimpleBean.class, SimpleContainerBean.class})
    @SalesforceField(name = "Value1")
    private Record value1;

    public Object getValue1() {
        return value1;
    }

    public void setValue1(Record value1) {
        this.value1 = value1;
    }
}

