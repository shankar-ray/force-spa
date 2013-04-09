/*
 * Copyright, 2012, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.core.ChildToParent;
import com.force.spa.core.SalesforceField;
import com.force.spa.core.SalesforceObject;

import java.util.Date;

@SalesforceObject
public class Contact {

    @SalesforceField(name = "Id")
    private String id;

    @SalesforceField(name = "CreatedDate")
    private Date createdDate;

    @ChildToParent
    @SalesforceField(name = "CreatedBy")
    private Name createdBy;

    @SalesforceField(name = "LastModifiedDate")
    private Date lastModifiedDate;

    @SalesforceField(name = "Name")
    private String name;

    @SalesforceField(name = "FirstName")
    private String firstName;

    @SalesforceField(name = "LastName")
    private String lastName;

    @SalesforceField(name = "Email")
    private String email;

    @SalesforceField(name = "Phone")
    private String phone;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Name getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Name createdBy) {
        this.createdBy = createdBy;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
