/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey.integration;

import java.util.List;

import com.force.spa.SalesforceField;
import com.force.spa.beans.UserBrief;

//TODO There should be an intermediate "User" object here too between brief and details
public class GuildUserDetails extends UserBrief {

    @SalesforceField(name = "GuildMemberships__r")
    private List<GuildMembership> memberships;

    @SalesforceField(name = "GuildApprentices__r")
    private List<GuildApprenticeship> apprentices;

    @SalesforceField(name = "GuildMasters__r")
    private List<GuildApprenticeship> masters;

    public List<GuildApprenticeship> getApprentices() {
        return apprentices;
    }

    public void setApprentices(List<GuildApprenticeship> apprentices) {
        this.apprentices = apprentices;
    }

    public List<GuildApprenticeship> getMasters() {
        return masters;
    }

    public void setMasters(List<GuildApprenticeship> masters) {
        this.masters = masters;
    }

    public List<GuildMembership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<GuildMembership> memberships) {
        this.memberships = memberships;
    }
}
