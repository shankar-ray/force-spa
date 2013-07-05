/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.ParentToChild;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

import java.util.List;

@SuppressWarnings("ALL")
@SalesforceObject
public class SimpleContainerBean {

    @SalesforceField(name = "Id")
    private String id;

    @ParentToChild
    @SalesforceField(name = "RelatedBeans")
    private List<SimpleBean> relatedBeans;

    @ParentToChild
    @SalesforceField(name = "MoreRelatedBeans")
    private SimpleBean[] moreRelatedBeans;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
