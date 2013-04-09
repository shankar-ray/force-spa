/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.core.SalesforceField;
import com.force.spa.core.SalesforceObject;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Date;

@SalesforceObject
public class DateTimeBean {

    @SalesforceField(name = "Id")
    private String id;

    @SalesforceField(name = "JavaDateAndTime")
    private Date javaDateAndTime;

    @SalesforceField(name = "JavaDateOnly")
    private Date javaDateOnly;

    @SalesforceField(name = "JodaDateAndTime")
    private DateTime jodaDateAndTime;

    @SalesforceField(name = "JodaDateOnly")
    private LocalDate jodaDateOnly;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getJavaDateAndTime() {
        return javaDateAndTime;
    }

    public void setJavaDateAndTime(Date javaDateAndTime) {
        this.javaDateAndTime = javaDateAndTime;
    }

    public Date getJavaDateOnly() {
        return javaDateOnly;
    }

    public void setJavaDateOnly(Date javaDateOnly) {
        this.javaDateOnly = javaDateOnly;
    }

    public DateTime getJodaDateAndTime() {
        return jodaDateAndTime;
    }

    public void setJodaDateAndTime(DateTime jodaDateAndTime) {
        this.jodaDateAndTime = jodaDateAndTime;
    }

    public LocalDate getJodaDateOnly() {
        return jodaDateOnly;
    }

    public void setJodaDateOnly(LocalDate jodaDateOnly) {
        this.jodaDateOnly = jodaDateOnly;
    }
}
