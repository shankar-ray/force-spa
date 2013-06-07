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
import org.joda.time.DateTime;

@SalesforceObject(name = "GuildApprenticeship__c")
public class GuildApprenticeship extends NamedRecord {

    @ChildToParent
    @SalesforceField(name = "Guild__c")
    private GuildBrief guild;

    @ChildToParent
    @SalesforceField(name = "Master__c")
    private GuildUserBrief master;

    @ChildToParent
    @SalesforceField(name = "Apprentice__c")
    private GuildUserBrief apprentice;

    @SalesforceField(name = "SinceDate__c")
    private DateTime sinceDate;

    public GuildUserBrief getApprentice() {
        return apprentice;
    }

    public void setApprentice(GuildUserBrief apprentice) {
        this.apprentice = apprentice;
    }

    public GuildBrief getGuild() {
        return guild;
    }

    public void setGuild(GuildBrief guild) {
        this.guild = guild;
    }

    public GuildUserBrief getMaster() {
        return master;
    }

    public void setMaster(GuildUserBrief master) {
        this.master = master;
    }

    public DateTime getSinceDate() {
        return sinceDate;
    }

    public void setSinceDate(DateTime sinceDate) {
        this.sinceDate = sinceDate;
    }
}
