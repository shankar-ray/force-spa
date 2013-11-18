/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import java.util.List;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

@SalesforceObject
@SuppressWarnings("UnusedDeclaration")
public class Account extends AccountBrief {

    @SalesforceField(name = "Notes")
    private List<Note> notes;

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        if (!super.equals(o)) return false;

        Account account = (Account) o;

        if (notes != null ? !notes.equals(account.notes) : account.notes != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (notes != null ? notes.hashCode() : 0);
        return result;
    }
}
