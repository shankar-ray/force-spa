/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.Share;

@SalesforceObject
public class AccountShare extends Share<Account> {

    // Deal with oddity that name for this field is different for Accounts.

    @Override
    @SalesforceField(name = "AccountAccessLevel")
    public AccessLevel getAccessLevel() {
        return super.getAccessLevel();
    }
}
