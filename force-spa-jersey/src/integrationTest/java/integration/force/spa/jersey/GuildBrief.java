/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package integration.force.spa.jersey;

import org.joda.time.DateTime;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.NamedRecord;

@SalesforceObject(name = "Guild__c")
public class GuildBrief extends NamedRecord {

    @SalesforceField(name = "Description__c")
    private String description;

    @SalesforceField(name = "FoundedDate__c")
    private DateTime foundedDate;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getFoundedDate() {
        return foundedDate;
    }

    public void setFoundedDate(DateTime foundedDate) {
        this.foundedDate = foundedDate;
    }
}
