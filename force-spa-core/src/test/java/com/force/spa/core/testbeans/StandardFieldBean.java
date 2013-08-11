/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.DetailedNamedRecord;
import com.force.spa.record.NamedRecord;

/**
 * A test bean that includes the standard Salesforce fields.
 */
@SalesforceObject
public class StandardFieldBean extends DetailedNamedRecord {

    private NamedRecord owner;

    @SalesforceField(name = "Owner")
    public NamedRecord getOwner() {
        return owner;
    }

    public void setOwner(NamedRecord owner) {
        this.owner = owner;
    }
}
