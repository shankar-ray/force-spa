/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.record;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

@SalesforceObject(name = "User")
public class UserBrief extends NamedRecord {

    @SalesforceField(name = "Username")
    private String username;

    @SalesforceField(name = "Email")
    private String email;

    @Override
    @SalesforceField(name = "Name", updatable = false, insertable = false)
    public String getName() {
        return super.getName();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

