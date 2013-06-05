/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.CreateRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.RecordAccessor;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordOperation;
import com.force.spa.RecordRequestException;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
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
        RecordOperation<String> createOperation = accessor.newCreateRecordOperation(contact);
        accessor.execute(createOperation);

        String id = createOperation.get();
        contact.setId(id);
        objects.add(contact);

        RecordOperation<Contact> getOperation = accessor.newGetRecordOperation(id, Contact.class);
        accessor.execute(getOperation);
        Contact contact2 = getOperation.get();
        assertThat(contact2.getId(), is(equalTo(id)));
        assertThat(contact2.getFirstName(), is(equalTo(contact.getFirstName())));
        assertThat(contact2.getLastName(), is(equalTo(contact.getLastName())));
    }

    @Test
    @Ignore("Batching is not readily available yet in server")
    public void testMultiplePatches() throws Exception {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        RecordOperation<String> createOperation = accessor.newCreateRecordOperation(contact);
        accessor.execute(createOperation);

        String id = createOperation.get();
        contact.setId(id);
        objects.add(contact);

        contact.setEmail("john.smith@acme.com");
        PatchRecordOperation firstPatchOperation = accessor.newPatchRecordOperation(id, contact);

        Contact contact2 = new Contact();
        contact2.setPhone("925-555-1212");
        PatchRecordOperation<Contact> secondPatchOperation = accessor.newPatchRecordOperation(id, contact2);
        accessor.execute(firstPatchOperation, secondPatchOperation);

        firstPatchOperation.get();  // Check for exceptions
        secondPatchOperation.get(); // Check for exceptions

        RecordOperation<Contact> getOperation = accessor.newGetRecordOperation(id, Contact.class);
        accessor.execute(getOperation);

        Contact contact3 = getOperation.get();
        assertThat(contact3.getId(), is(equalTo(id)));
        assertThat(contact3.getFirstName(), is(equalTo(contact.getFirstName())));
        assertThat(contact3.getLastName(), is(equalTo(contact.getLastName())));
        assertThat(contact3.getEmail(), is(equalTo(contact.getEmail())));
        assertThat(contact3.getPhone(), is(equalTo(contact2.getPhone())));
    }

    @Test
    @Ignore("Batching is not readily available yet in server")
    @SuppressWarnings("unchecked")
    public void testLotsOfCreates() throws Exception {
        int numberOfCreates = 25;
        long before = System.currentTimeMillis();
        List<RecordOperation<?>> createOperations = new ArrayList<RecordOperation<?>>();
        for (int i = 0; i < numberOfCreates; i++) {
            Contact contact = new Contact();
            contact.setFirstName("John" + i);
            contact.setLastName("Smith" + i);
            createOperations.add(accessor.newCreateRecordOperation(contact));
        }
        accessor.execute(createOperations);
        long after = System.currentTimeMillis();
        System.out.println("Elapsed create time: " + (after - before));

        List<RecordOperation<?>> getOperations = new ArrayList<RecordOperation<?>>();
        for (int i = 0; i < numberOfCreates; i++) {
            String id = ((CreateRecordOperation<Contact>) createOperations.get(i)).get();
            getOperations.add(accessor.newGetRecordOperation(id, Contact.class));
        }
        accessor.execute(getOperations);

        for (int i = 0; i < numberOfCreates; i++) {
            Contact originalContact = ((CreateRecordOperation<Contact>) createOperations.get(i)).getRecord();
            Contact persistentContact = ((GetRecordOperation<Contact>) getOperations.get(i)).get();
            assertThat(originalContact.getFirstName(), is(equalTo(persistentContact.getFirstName())));
            assertThat(originalContact.getLastName(), is(equalTo(persistentContact.getLastName())));
        }
    }

    @Test
    public void testDelete() throws Exception {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        RecordOperation<String> createOperation = accessor.newCreateRecordOperation(contact);
        accessor.execute(createOperation);

        String id = createOperation.get();
        contact.setId(id);
        objects.add(contact);

        RecordOperation<Contact> getOperation = accessor.newGetRecordOperation(id, Contact.class);
        accessor.execute(getOperation);

        Contact contact2 = getOperation.get();
        assertThat(contact2.getId(), is(equalTo(id)));

        accessor.execute(accessor.newDeleteRecordOperation(id, Contact.class));
        objects.remove(contact);

        getOperation = accessor.newGetRecordOperation(id, Contact.class);
        accessor.execute(getOperation);
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
