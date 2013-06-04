/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.CreateRecordOperation;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordOperation;
import com.force.spa.RecordRequestException;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class RecordAccessorBatchIntegrationTest {
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
    public void testSingleCreateAndGet() throws Exception {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        List<RecordOperation<?>> operations = new ArrayList<RecordOperation<?>>();
        RecordOperation<String> createOperation = accessor.newCreateRecordOperation(contact);
        operations.add(createOperation);
        accessor.execute(operations);

        String id = createOperation.get();
        contact.setId(id);
        objects.add(contact);

        operations = new ArrayList<RecordOperation<?>>();
        RecordOperation<Contact> getOperation = accessor.newGetRecordOperation(id, Contact.class);
        operations.add(getOperation);
        accessor.execute(operations);
        Contact contact2 = getOperation.get();
        assertThat(contact2.getId(), is(equalTo(id)));
        assertThat(contact2.getFirstName(), is(equalTo(contact.getFirstName())));
        assertThat(contact2.getLastName(), is(equalTo(contact.getLastName())));
    }

    @Test
    public void testMultiplePatches() throws Exception {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        List<RecordOperation<?>> operations = new ArrayList<RecordOperation<?>>();
        RecordOperation<String> createOperation = accessor.newCreateRecordOperation(contact);
        operations.add(createOperation);
        accessor.execute(operations);

        String id = createOperation.get();
        contact.setId(id);
        objects.add(contact);

        operations = new ArrayList<RecordOperation<?>>();
        contact.setEmail("john.smith@acme.com");
        operations.add(accessor.newPatchRecordOperation(id, contact));

        Contact contact2 = new Contact();
        contact2.setPhone("925-555-1212");
        operations.add(accessor.newPatchRecordOperation(id, contact2));

        accessor.execute(operations);

        operations = new ArrayList<RecordOperation<?>>();
        RecordOperation<Contact> getOperation = accessor.newGetRecordOperation(id, Contact.class);
        operations.add(getOperation);
        accessor.execute(operations);

        Contact contact3 = getOperation.get();
        assertThat(contact3.getId(), is(equalTo(id)));
        assertThat(contact3.getFirstName(), is(equalTo(contact.getFirstName())));
        assertThat(contact3.getLastName(), is(equalTo(contact.getLastName())));
        assertThat(contact3.getEmail(), is(equalTo(contact.getEmail())));
        assertThat(contact3.getPhone(), is(equalTo(contact2.getPhone())));
    }

    @Test
    public void testDelete() throws Exception {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        List<RecordOperation<?>> operations = new ArrayList<RecordOperation<?>>();
        RecordOperation<String> createOperation = accessor.newCreateRecordOperation(contact);
        operations.add(createOperation);
        accessor.execute(operations);

        String id = createOperation.get();
        contact.setId(id);
        objects.add(contact);

        operations = new ArrayList<RecordOperation<?>>();
        RecordOperation<Contact> getOperation = accessor.newGetRecordOperation(id, Contact.class);
        operations.add(getOperation);
        accessor.execute(operations);

        Contact contact2 = getOperation.get();
        assertThat(contact2.getId(), is(equalTo(id)));

        operations = new ArrayList<RecordOperation<?>>();
        operations.add(accessor.newDeleteRecordOperation(id, Contact.class));
        accessor.execute(operations);
        objects.remove(contact);

        operations = new ArrayList<RecordOperation<?>>();
        getOperation = accessor.newGetRecordOperation(id, Contact.class);
        operations.add(getOperation);
        accessor.execute(operations);
        try {
            getOperation.get();
            fail("Didn't get expected RecordNotFoundException");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RecordRequestException) {
                // This is expected.
            } else {
                throw e;
            }
        }
    }

    @Test
    public void testDeleteNonexistentId() {
        try {
            accessor.delete("0123456789012345", Contact.class);
            fail("Didn't get expected RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            // This is expected
        }
    }
}
