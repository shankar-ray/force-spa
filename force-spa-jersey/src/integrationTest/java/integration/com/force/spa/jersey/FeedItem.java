/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import com.force.spa.Polymorphic;
import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.Record;
import com.force.spa.beans.UserBrief;

@SalesforceObject
public class FeedItem extends Record {
    @SalesforceField(name = "Body")
    private String body;

    @Polymorphic({Account.class, UserBrief.class})
    @SalesforceField(name = "Parent")
    private Record parent;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Record getParent() {
        return parent;
    }

    public void setParent(Record parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FeedItem feedItem = (FeedItem) o;

        if (body != null ? !body.equals(feedItem.body) : feedItem.body != null) return false;
        if (parent != null ? !parent.equals(feedItem.parent) : feedItem.parent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
}
