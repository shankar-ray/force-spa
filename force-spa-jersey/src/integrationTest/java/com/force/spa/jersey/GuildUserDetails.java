/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.ParentToChild;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

import java.util.List;

@SalesforceObject(name = "User")
public class GuildUserDetails extends GuildUser {

    @ParentToChild
    @SalesforceField(name= "GuildMemberships__r")
    private List<GuildMembership> memberships;

    @ParentToChild
    @SalesforceField(name= "GuildApprentices__r")
    private List<GuildApprenticeship> apprentices;

    @ParentToChild
    @SalesforceField(name= "GuildMasters__r")
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
