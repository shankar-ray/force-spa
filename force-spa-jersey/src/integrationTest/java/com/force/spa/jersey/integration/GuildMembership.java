/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey.integration;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.NamedRecord;
import com.force.spa.beans.UserBrief;

@SalesforceObject(name = "GuildMembership__c")
public class GuildMembership extends NamedRecord {

    @SalesforceField(name = "Guild__c")
    private GuildBrief guild;

    @SalesforceField(name = "User__c")
    private UserBrief user;

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

    public UserBrief getUser() {
        return user;
    }

    public void setUser(UserBrief user) {
        this.user = user;
    }
}
