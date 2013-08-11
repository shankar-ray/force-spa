/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceObject;
import com.force.spa.record.Record;

import javax.persistence.Transient;

@SalesforceObject
@SuppressWarnings("UnusedDeclaration")
public class TransientFieldBean extends Record {

    @Transient
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
