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
@SuppressWarnings("UnusedDeclaration")
public class AccountShare extends Share<Account> {

    // Deal with oddity that several names are different for Account shares.

    @Override
    @SalesforceField(name = "AccountAccessLevel")
    public AccessLevel getAccessLevel() {
        return super.getAccessLevel();
    }

    @Override
    @SalesforceField(name = "Account")
    public Account getParent() {
        return super.getParent();
    }
}
