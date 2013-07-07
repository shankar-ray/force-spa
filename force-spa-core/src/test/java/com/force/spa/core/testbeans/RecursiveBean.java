/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

/**
 * A test bean that is helps test recursive type references.
 */
@SuppressWarnings("ALL")
@SalesforceObject
public class RecursiveBean {

    @SalesforceField(name = "Id")
    private String id;

    private RecursiveBean recursiveBean;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SalesforceField(name = "RecursiveBean")
    public RecursiveBean getRecursiveBean() {
        return recursiveBean;
    }

    public void setRecursiveBean(RecursiveBean recursiveBean) {
        this.recursiveBean = recursiveBean;
    }
}
