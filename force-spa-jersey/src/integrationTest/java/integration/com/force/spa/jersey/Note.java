/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import org.joda.time.DateTime;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;
import com.force.spa.beans.Record;
import com.force.spa.beans.UserBrief;

@SalesforceObject
public class Note extends Record {

    @SalesforceField(name = "CreatedBy")
    private UserBrief createdBy;

    @SalesforceField(name = "CreatedDate")
    private DateTime createdDate;

    @SalesforceField(name = "Title")
    private String title;

    @SalesforceField(name = "Body")
    private String body;

    @SalesforceField(name = "Parent")
    private Record parent;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public UserBrief getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserBrief createdBy) {
        this.createdBy = createdBy;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Record getParent() {
        return parent;
    }

    public void setParent(Record parent) {
        this.parent = parent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Note note = (Note) o;

        if (body != null ? !body.equals(note.body) : note.body != null) return false;
        if (createdBy != null ? !createdBy.equals(note.createdBy) : note.createdBy != null) return false;
        if (createdDate != null ? !createdDate.equals(note.createdDate) : note.createdDate != null) return false;
        if (parent != null ? !parent.equals(note.parent) : note.parent != null) return false;
        if (title != null ? !title.equals(note.title) : note.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}
