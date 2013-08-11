/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.Record;

import java.util.List;

@SalesforceObject
@SuppressWarnings("UnusedDeclaration")
public class SimpleContainerBean extends Record {

    @SalesforceField(name = "RelatedBeans")
    private List<SimpleBean> relatedBeans;

    @SalesforceField(name = "MoreRelatedBeans")
    private SimpleBean[] moreRelatedBeans;

    public List<SimpleBean> getRelatedBeans() {
        return relatedBeans;
    }

    public void setRelatedBeans(List<SimpleBean> relatedBeans) {
        this.relatedBeans = relatedBeans;
    }

    public SimpleBean[] getMoreRelatedBeans() {
        return moreRelatedBeans;
    }

    public void setMoreRelatedBeans(SimpleBean[] moreRelatedBeans) {
        this.moreRelatedBeans = moreRelatedBeans;
    }
}
