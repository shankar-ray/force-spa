/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.RecordNotFoundException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class RecordAccessorIntegrationTest extends AbstractRecordAccessorIntegrationTest {

    @Test
    public void testSimpleCreate() {
        Guild guild = createTestGuild();

        Guild guild2 = accessor.get(guild.getId(), Guild.class);
        assertThat(guild2.getId(), is(equalTo(guild.getId())));
        assertThat(guild2.getName(), is(equalTo(guild.getName())));
        assertThat(guild2.getDescription(), is(equalTo(guild.getDescription())));
    }

    @Test
    public void testCreateWithRelationshipById() {
        Guild guild = createTestGuild();
        GuildUser user = accessor.get(authorizationConnector.getUsedId(), GuildUser.class);

        GuildMembership membership = new GuildMembership();
        membership.setGuild(guild);
        membership.setUser(user);
        membership.setLevel("Apprentice");
        String id = accessor.create(membership);
        membership.setId(id);

        GuildMembership membership2 = accessor.get(membership.getId(), GuildMembership.class);
        assertThat(membership2.getId(), is(equalTo(membership.getId())));
        assertThat(membership2.getLevel(), is(equalTo(membership.getLevel())));
        assertThat(membership2.getGuild().getId(), is(equalTo(membership.getGuild().getId())));
        assertThat(membership2.getUser().getId(), is(equalTo(membership.getUser().getId())));
    }

    @Test
    public void testCreateWithRelationshipByExternalId() {
        Guild guild = createTestGuild();
        GuildUser user = accessor.get(authorizationConnector.getUsedId(), GuildUser.class);
        GuildUser userByUsername = new GuildUser();
        userByUsername.setUsername(user.getUsername());

        GuildMembership membership = new GuildMembership();
        membership.setGuild(guild);
        membership.setUser(userByUsername);
        membership.setLevel("Apprentice");
        String id = accessor.create(membership);
        membership.setId(id);

        GuildMembership membership2 = accessor.get(membership.getId(), GuildMembership.class);
        assertThat(membership2.getId(), is(equalTo(membership.getId())));
        assertThat(membership2.getLevel(), is(equalTo(membership.getLevel())));
        assertThat(membership2.getGuild().getId(), is(equalTo(membership.getGuild().getId())));
        assertThat(membership2.getUser().getId(), is(equalTo(user.getId())));
    }

    @Test
    public void testPatch() {
        Guild guild = createTestGuild();

        guild.setName("Ultimate Speed Cyclists");
        accessor.patch(guild.getId(), guild);

        Guild guild2 = new Guild();
        guild2.setDescription("A guild for really fast bicycle racers.");
        accessor.patch(guild.getId(), guild2);

        Guild guild3 = accessor.get(guild.getId(), Guild.class);
        assertThat(guild3.getId(), is(equalTo(guild.getId())));
        assertThat(guild3.getName(), is(equalTo(guild.getName())));
        assertThat(guild3.getDescription(), is(equalTo(guild2.getDescription())));
    }

    @Test
    public void testDelete() {
        Guild guild = createTestGuild();


        Guild guild2 = accessor.get(guild.getId(), Guild.class);
        assertThat(guild2.getId(), is(equalTo(guild.getId())));

        accessor.delete(guild.getId(), Guild.class);
        objects.remove(guild);

        try {
            accessor.get(guild.getId(), Guild.class);
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
