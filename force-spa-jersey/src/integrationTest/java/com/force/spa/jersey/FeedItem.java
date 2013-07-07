/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.Polymorphic;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.Record;
import com.force.spa.record.UserBrief;

@SalesforceObject
public class FeedItem extends Record {
    @SalesforceField(name = "Body")
    private String body;

    @Polymorphic({GuildBrief.class, UserBrief.class})
    @SalesforceField(name = "Parent")
    private Record parent;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Record getParent() {
        return parent;
    }

    public void setParent(Record parent) {
        this.parent = parent;
    }
}
