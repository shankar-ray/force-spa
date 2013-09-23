/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import java.util.List;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.NamedRecord;

@SuppressWarnings("UnusedDeclaration")
@SalesforceObject(name = "namespace__CustomBean__c")
public class CustomBean extends NamedRecord {

    @SalesforceField(name = "namespace__Value1__c")
    private String value1;

    @SalesforceField(name = "namespace__Value2__c")
    private String value2;

    @SalesforceField(name = "Value3__c")
    private String value3;

    @SalesforceField(name = "RelatedBeans__r")
    private List<SimpleBean> relatedBeans;

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
