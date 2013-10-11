/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.beans;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserBrief)) return false;
        if (!super.equals(o)) return false;

        UserBrief userBrief = (UserBrief) o;

        if (email != null ? !email.equals(userBrief.email) : userBrief.email != null) return false;
        if (username != null ? !username.equals(userBrief.username) : userBrief.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
    }
}

