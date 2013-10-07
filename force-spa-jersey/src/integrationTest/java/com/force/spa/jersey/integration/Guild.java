/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey.integration;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.UserBrief;

@SalesforceObject(name = "Guild__c")
public class Guild extends GuildBrief {

    @SalesforceField(name = "President__c")
    private UserBrief president;

    public UserBrief getPresident() {
        return president;
    }

    public void setPresident(UserBrief president) {
        this.president = president;
    }
}
