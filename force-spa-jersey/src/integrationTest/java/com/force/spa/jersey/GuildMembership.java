/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.ChildToParent;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.record.NamedRecord;

@SalesforceObject(name = "GuildMembership__c")
public class GuildMembership extends NamedRecord {

    @ChildToParent
    @SalesforceField(name = "Guild__c")
    private GuildBrief guild;

    @ChildToParent
    @SalesforceField(name = "User__c")
    private GuildUserBrief user;

    @SalesforceField(name = "Level__c")
    private String level;

    public GuildBrief getGuild() {
        return guild;
    }

    public void setGuild(GuildBrief guild) {
        this.guild = guild;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public GuildUserBrief getUser() {
        return user;
    }

    public void setUser(GuildUserBrief user) {
        this.user = user;
    }
}
