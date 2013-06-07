/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.RecordAccessor;
import com.force.spa.RecordNotFoundException;
import org.junit.After;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

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
        Guild guild = new Guild();
        guild.setName("Speed Cyclists");
        guild.setDescription("A guild for bicycle racers.");
        String id = accessor.create(guild);
        guild.setId(id);
        objects.add(guild);

        Guild guild2 = accessor.get(id, Guild.class);
        assertThat(guild2.getId(), is(equalTo(id)));
        assertThat(guild2.getName(), is(equalTo(guild.getName())));
        assertThat(guild2.getDescription(), is(equalTo(guild.getDescription())));
    }

    @Test
    public void testPatch() {
        Guild guild = new Guild();
        guild.setName("Speed Cyclists");
        guild.setDescription("A guild for bicycle racers.");
        String id = accessor.create(guild);
        guild.setId(id);
        objects.add(guild);

        guild.setName("Ultimate Speed Cyclists");
        accessor.patch(id, guild);

        Guild guild2 = new Guild();
        guild2.setDescription("A guild for really fast bicycle racers.");
        accessor.patch(id, guild2);

        Guild guild3 = accessor.get(id, Guild.class);
        assertThat(guild3.getId(), is(equalTo(id)));
        assertThat(guild3.getName(), is(equalTo(guild.getName())));
        assertThat(guild3.getDescription(), is(equalTo(guild2.getDescription())));
    }

    @Test
    public void testDelete() {
        Guild guild = new Guild();
        guild.setName("Speed Cyclists");
        guild.setDescription("A guild for bicycle racers.");
        String id = accessor.create(guild);
        guild.setId(id);
        objects.add(guild);

        Guild guild2 = accessor.get(id, Guild.class);
        assertThat(guild2.getId(), is(equalTo(id)));

        accessor.delete(id, Guild.class);
        objects.remove(guild);

        try {
            accessor.get(id, Guild.class);
            fail("Didn't get expected RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            // This is expected
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
