/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import java.util.Map;

import com.force.spa.SalesforceField;
import com.force.spa.beans.Record;

public class HideAttributesBean extends Record {

    @Override
    @SalesforceField(name = "NotAttributes")
    public Map<String, String> getAttributes() {
        return super.getAttributes();
    }
}
