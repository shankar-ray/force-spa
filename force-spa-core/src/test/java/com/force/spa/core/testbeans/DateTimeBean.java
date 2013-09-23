/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.Record;

@SalesforceObject
public class DateTimeBean extends Record {

    @SalesforceField(name = "JavaDateAndTime")
    private Date javaDateAndTime;

    @SalesforceField(name = "JavaDateOnly")
    private Date javaDateOnly;

    @SalesforceField(name = "JodaDateAndTime")
    private DateTime jodaDateAndTime;

    @SalesforceField(name = "JodaDateOnly")
    private LocalDate jodaDateOnly;

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
