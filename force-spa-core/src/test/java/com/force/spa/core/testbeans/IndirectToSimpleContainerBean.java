/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.NamedRecord;

@SalesforceObject
@SuppressWarnings("UnusedDeclaration")
public class IndirectToSimpleContainerBean extends NamedRecord {

    @SalesforceField(name = "ContainerBean")
    private SimpleContainerBean containerBean;

    public SimpleContainerBean getContainerBean() {
        return containerBean;
    }

    public void setContainerBean(SimpleContainerBean containerBean) {
        this.containerBean = containerBean;
    }
}
