/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.NamedRecord;

@SalesforceObject(name = "User")
public class GuildUserBrief extends NamedRecord {

    @SalesforceField(name = "Title")
    private String title;

    @SalesforceField(name = "Email")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
