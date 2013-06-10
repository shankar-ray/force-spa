/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.CreateRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.RecordNotFoundException;
import com.force.spa.RecordOperation;
import com.force.spa.RecordRequestException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class RecordAccessorBatchIntegrationTest extends AbstractRecordAccessorIntegrationTest {

    @Test
    public void testSingleCreateAndGet() throws Exception {
        Guild guild = new Guild();
        guild.setName("Speed Cyclists");
        guild.setDescription("A guild for bicycle racers.");
        RecordOperation<String> createOperation = accessor.newCreateRecordOperation(guild);
        accessor.execute(createOperation);

        String id = createOperation.get();
        guild.setId(id);
        objects.add(guild);

        RecordOperation<Guild> getOperation = accessor.newGetRecordOperation(id, Guild.class);
        accessor.execute(getOperation);
        Guild guild2 = getOperation.get();
        assertThat(guild2.getId(), is(equalTo(id)));
        assertThat(guild2.getName(), is(equalTo(guild.getName())));
        assertThat(guild2.getDescription(), is(equalTo(guild.getDescription())));
    }

    @Test
    public void testMultiplePatches() throws Exception {
        Guild guild = new Guild();
        guild.setName("Speed Cyclists");
        guild.setDescription("A guild for bicycle racers.");
        RecordOperation<String> createOperation = accessor.newCreateRecordOperation(guild);
        accessor.execute(createOperation);

        String id = createOperation.get();
        guild.setId(id);
        objects.add(guild);

        guild.setName("Ultimate Speed Cyclists");
        PatchRecordOperation firstPatchOperation = accessor.newPatchRecordOperation(id, guild);

        Guild guild2 = new Guild();
        guild2.setDescription("A guild for really fast bicycle racers.");
        PatchRecordOperation<Guild> secondPatchOperation = accessor.newPatchRecordOperation(id, guild2);
        accessor.execute(firstPatchOperation, secondPatchOperation);

        firstPatchOperation.get();  // Check for exceptions
        secondPatchOperation.get(); // Check for exceptions

        RecordOperation<Guild> getOperation = accessor.newGetRecordOperation(id, Guild.class);
        accessor.execute(getOperation);

        Guild guild3 = getOperation.get();
        assertThat(guild3.getId(), is(equalTo(id)));
        assertThat(guild3.getId(), is(equalTo(id)));
        assertThat(guild3.getName(), is(equalTo(guild.getName())));
        assertThat(guild3.getDescription(), is(equalTo(guild2.getDescription())));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLotsOfCreates() throws Exception {
        int numberOfCreates = 25;
        long before = System.currentTimeMillis();
        List<RecordOperation<?>> createOperations = new ArrayList<RecordOperation<?>>();
        for (int i = 0; i < numberOfCreates; i++) {
            Guild guild = new Guild();
            guild.setName("Speed Cyclists" + i);
            guild.setDescription("A guild for bicycle racers." + i);
            createOperations.add(accessor.newCreateRecordOperation(guild));
        }
        accessor.execute(createOperations);
        long after = System.currentTimeMillis();
        System.out.println("Elapsed create time: " + (after - before));

        List<RecordOperation<?>> getOperations = new ArrayList<RecordOperation<?>>();
        for (int i = 0; i < numberOfCreates; i++) {
            String id = ((CreateRecordOperation<Guild>) createOperations.get(i)).get();
            getOperations.add(accessor.newGetRecordOperation(id, Guild.class));
        }
        accessor.execute(getOperations);

        for (int i = 0; i < numberOfCreates; i++) {
            Guild originalGuild = ((CreateRecordOperation<Guild>) createOperations.get(i)).getRecord();
            Guild persistentGuild = ((GetRecordOperation<Guild>) getOperations.get(i)).get();
            assertThat(originalGuild.getName(), is(equalTo(persistentGuild.getName())));
            assertThat(originalGuild.getDescription(), is(equalTo(persistentGuild.getDescription())));
        }
    }

    @Test
    public void testDelete() throws Exception {
        Guild guild = new Guild();
        guild.setName("Speed Cyclists");
        guild.setDescription("A guild for bicycle racers.");
        RecordOperation<String> createOperation = accessor.newCreateRecordOperation(guild);
        accessor.execute(createOperation);

        String id = createOperation.get();
        guild.setId(id);
        objects.add(guild);

        RecordOperation<Guild> getOperation = accessor.newGetRecordOperation(id, Guild.class);
        accessor.execute(getOperation);

        Guild guild2 = getOperation.get();
        assertThat(guild2.getId(), is(equalTo(id)));

        accessor.execute(accessor.newDeleteRecordOperation(id, Guild.class));
        objects.remove(guild);

        getOperation = accessor.newGetRecordOperation(id, Guild.class);
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
            accessor.delete("0123456789012345", Guild.class);
            fail("Didn't get expected RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            // This is expected
        }
    }
}
