/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey.integration;

import org.joda.time.DateTime;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.NamedRecord;
import com.force.spa.beans.UserBrief;

@SalesforceObject(name = "GuildApprenticeship__c")
public class GuildApprenticeship extends NamedRecord {

    @SalesforceField(name = "Guild__c")
    private GuildBrief guild;

    @SalesforceField(name = "Master__c")
    private UserBrief master;

    @SalesforceField(name = "Apprentice__c")
    private UserBrief apprentice;

    @SalesforceField(name = "SinceDate__c")
    private DateTime sinceDate;

    public UserBrief getApprentice() {
        return apprentice;
    }

    public void setApprentice(UserBrief apprentice) {
        this.apprentice = apprentice;
    }

    public GuildBrief getGuild() {
        return guild;
    }

    public void setGuild(GuildBrief guild) {
        this.guild = guild;
    }

    public UserBrief getMaster() {
        return master;
    }

    public void setMaster(UserBrief master) {
        this.master = master;
    }

    public DateTime getSinceDate() {
        return sinceDate;
    }

    public void setSinceDate(DateTime sinceDate) {
        this.sinceDate = sinceDate;
    }
}
