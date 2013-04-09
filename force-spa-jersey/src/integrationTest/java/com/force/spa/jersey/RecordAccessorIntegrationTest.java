/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.core.RecordAccessor;
import org.junit.After;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class RecordAccessorIntegrationTest {
    private RecordAccessor accessor = new RecordAccessorFactory().newInstance();
    private Set<Object> objects = new HashSet<Object>();

    @After
    public void deleteTestObjects() {
        for (Object object : objects) {
            try {
                accessor.delete(object);
            } catch (Exception e) {
                System.err.println("Failed to clean up object: " + e.toString());
            }
        }
    }

    @Test
    public void testCreateAndFind() {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        String id = accessor.create(contact);
        contact.setId(id);
        objects.add(contact);

        Contact contact2 = accessor.get(id, Contact.class);
        assertThat(contact2.getId(), is(equalTo(id)));
        assertThat(contact2.getFirstName(), is(equalTo(contact.getFirstName())));
        assertThat(contact2.getLastName(), is(equalTo(contact.getLastName())));
    }

    @Test
    public void testPatch() {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        String id = accessor.create(contact);
        contact.setId(id);
        objects.add(contact);

        contact.setEmail("john.smith@acme.com");
        accessor.patch(id, contact);

        Contact contact2 = new Contact();
        contact2.setPhone("925-555-1212");
        accessor.patch(id, contact2);

        Contact contact3 = accessor.get(id, Contact.class);
        assertThat(contact3.getId(), is(equalTo(id)));
        assertThat(contact3.getFirstName(), is(equalTo(contact.getFirstName())));
        assertThat(contact3.getLastName(), is(equalTo(contact.getLastName())));
        assertThat(contact3.getEmail(), is(equalTo(contact.getEmail())));
        assertThat(contact3.getPhone(), is(equalTo(contact2.getPhone())));
    }

    @Test
    public void testDelete() {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        String id = accessor.create(contact);
        contact.setId(id);
        objects.add(contact);

        Contact contact2 = accessor.get(id, Contact.class);
        assertThat(contact2.getId(), is(equalTo(id)));

        accessor.delete(id, Contact.class);
        objects.remove(contact);

        Contact contact3 = accessor.get(id, Contact.class);
        assertThat(contact3, is(nullValue()));
    }
}
