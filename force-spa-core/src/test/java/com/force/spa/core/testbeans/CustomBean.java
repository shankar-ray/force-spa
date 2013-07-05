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
@SalesforceObject(name = "namespace__CustomBean__c")
public class CustomBean {

    @SalesforceField(name = "Id")
    private String id;

    @SalesforceField(name = "Name")
    private String name;

    @SalesforceField(name = "namespace__Value1__c")
    private String value1;

    @SalesforceField(name = "namespace__Value2__c")
    private String value2;

    @SalesforceField(name = "Value3__c")
    private String value3;

    @ParentToChild
    @SalesforceField(name = "RelatedBeans__c")
    private List<SimpleBean> relatedBeans;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SimpleBean> getRelatedBeans() {
        return relatedBeans;
    }

    public void setRelatedBeans(List<SimpleBean> relatedBeans) {
        this.relatedBeans = relatedBeans;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }
}
