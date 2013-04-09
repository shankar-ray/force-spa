/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.core.ChildToParent;
import com.force.spa.core.SalesforceField;
import com.force.spa.core.SalesforceObject;

/**
 * A test bean that is helps test recursive type references.
 */
@SalesforceObject
public class RecursiveBean {

    @SalesforceField(name = "Id")
    private String id;

    @ChildToParent
    @SalesforceField(name = "RecursiveBean")
    private RecursiveBean recursiveBean;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RecursiveBean getRecursiveBean() {
        return recursiveBean;
    }

    public void setRecursiveBean(RecursiveBean recursiveBean) {
        this.recursiveBean = recursiveBean;
    }
}
