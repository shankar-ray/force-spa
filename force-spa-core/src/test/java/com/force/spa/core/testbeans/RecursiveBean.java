/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.Record;

/**
 * A test bean that is helps test recursive type references.
 */
@SalesforceObject
public class RecursiveBean extends Record {

    @SalesforceField(name = "RecursiveBean")
    private RecursiveBean recursiveBean;
}
