/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

import java.util.List;

@SalesforceObject(name = "Guild__c")
public class GuildDetails extends Guild {

    @SalesforceField(name = "GuildMemberships__r")
    private List<GuildMembership> memberships;

    @SalesforceField(name = "GuildApprentices__r")
    private List<GuildApprenticeship> apprenticeships;

    public List<GuildApprenticeship> getApprenticeships() {
        return apprenticeships;
    }

    public void setApprenticeships(List<GuildApprenticeship> apprenticeships) {
        this.apprenticeships = apprenticeships;
    }

    public List<GuildMembership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<GuildMembership> memberships) {
        this.memberships = memberships;
    }
}
