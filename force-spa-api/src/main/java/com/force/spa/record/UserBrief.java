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

    private String email;
    private String username;

    @Override
    @SuppressWarnings("EmptyMethod")
    @SalesforceField(name = "Name", updatable = false, insertable = false)
    public String getName() {
        return super.getName();
    }

    @SalesforceField(name = "Email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @SalesforceField(name = "Username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

