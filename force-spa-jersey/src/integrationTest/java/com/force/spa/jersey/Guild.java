/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.ChildToParent;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.DetailedNamedRecord;
import org.joda.time.DateTime;

@SalesforceObject(name = "Guild__c")
public class Guild extends GuildBrief {

    @ChildToParent
    @SalesforceField(name = "President__c")
    private GuildUserBrief president;

    public GuildUserBrief getPresident() {
        return president;
    }

    public void setPresident(GuildUserBrief president) {
        this.president = president;
    }
}
