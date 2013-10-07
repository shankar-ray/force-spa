/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import java.util.List;

import com.force.spa.Polymorphic;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.Record;

@SalesforceObject
public class PolymorphicListFieldBean extends Record {

    @Polymorphic({SimpleBean.class, ExplicitlyNamedBean.class})
    @SalesforceField(name = "Values")
    private List<Record> values;

    public List<Record> getValues() {
        return values;
    }

    public void setValues(List<Record> values) {
        this.values = values;
    }
}

