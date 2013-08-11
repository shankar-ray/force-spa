/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.RecordNotFoundException;
import com.force.spa.record.GroupBrief;
import com.force.spa.record.Share;
import com.force.spa.record.UserBrief;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class RecordAccessorIntegrationTest extends AbstractRecordAccessorIntegrationTest {

    @Test
    public void testSimpleCreate() {
        Guild guild = createGuild();

        Guild guild2 = accessor.get(guild.getId(), Guild.class);
        assertThat(guild2.getId(), is(equalTo(guild.getId())));
        assertThat(guild2.getName(), is(equalTo(guild.getName())));
        assertThat(guild2.getDescription(), is(equalTo(guild.getDescription())));
    }

    @Test
    public void testCreateWithRelationshipById() {
        Guild guild = createGuild();
        UserBrief user = accessor.get(authorizationConnector.getUsedId(), UserBrief.class);

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
        Guild guild = createGuild();
        UserBrief user = accessor.get(authorizationConnector.getUsedId(), UserBrief.class);
        UserBrief userByUsername = new UserBrief();
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
        Guild guild = createGuild();

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
        Guild guild = createGuild();


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

    @Test
    public void testGuildSharingRow() {
        Guild guild = createGuild();
        GroupBrief group = getAllInternalUsersGroup();

        GuildShare share = new GuildShare();
        share.setAccessLevel(Share.AccessLevel.Edit);
        share.setRowCause(Share.RowCause.Manual);
        share.setParent(guild);
        share.setUserOrGroupId(group.getId());

        String shareId = accessor.create(share);

        GuildShare share2 = accessor.get(shareId, GuildShare.class);
        assertThat(share2.getId(), is(equalTo(shareId)));
        assertThat(share2.getAccessLevel(), is(equalTo(share.getAccessLevel())));
        assertThat(share2.getRowCause(), is(equalTo(share.getRowCause())));
        assertThat(share2.getParent().getId(), is(equalTo(share.getParent().getId())));
        assertThat(share2.getUserOrGroupId(), is(equalTo(share.getUserOrGroupId())));
    }


    @Test
    public void testFeedItemPolymorphism() {
        Guild guild = createGuild();

        FeedItem feedItem = new FeedItem();
        feedItem.setParent(guild);
        feedItem.setBody("Feed item body");
        String feedItemId = accessor.create(feedItem);

        FeedItem feedItem2 = accessor.get(feedItemId, FeedItem.class);
        assertThat(feedItem2.getParent(), is(instanceOf(GuildBrief.class)));
    }

    private GroupBrief getAllInternalUsersGroup() {
        String soql = "select * from Group where DeveloperName=\'AllInternalUsers\'";
        List<GroupBrief> groups = accessor.createQuery(soql, GroupBrief.class).execute();
        if (groups.size() == 0) {
            return null;
        }
        return groups.get(0);
    }
}
