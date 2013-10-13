/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import java.security.SecureRandom;
import java.util.List;

import org.junit.Test;

import com.force.spa.AuthorizationConnector;
import com.force.spa.RecordNotFoundException;
import com.force.spa.beans.GroupBrief;
import com.force.spa.beans.Share;
import com.force.spa.beans.UserBrief;
import com.force.spa.jersey.PasswordAuthorizationConnector;
import com.force.spa.metadata.ObjectMetadata;

public class RecordAccessorIntegrationTest extends AbstractRecordAccessorIntegrationTest {
    private static final SecureRandom secureRandom = new SecureRandom();

    @Test
    public void testSimpleCreate() {
        Guild guild = createGuild();

        Guild guild2 = getRecordAccessor().get(guild.getId(), Guild.class);
        assertThat(guild2.getId(), is(equalTo(guild.getId())));
        assertThat(guild2.getName(), is(equalTo(guild.getName())));
        assertThat(guild2.getDescription(), is(equalTo(guild.getDescription())));
    }

    @Test
    public void testCreateWithRelationshipById() {
        Guild guild = createGuild();
        String userId = getCurrentUserId();
        UserBrief user = getRecordAccessor().get(userId, UserBrief.class);

        GuildMembership membership = new GuildMembership();
        membership.setGuild(guild);
        membership.setUser(user);
        membership.setLevel("Apprentice");
        String id = getRecordAccessor().create(membership);
        membership.setId(id);

        GuildMembership membership2 = getRecordAccessor().get(membership.getId(), GuildMembership.class);
        assertThat(membership2.getId(), is(equalTo(membership.getId())));
        assertThat(membership2.getLevel(), is(equalTo(membership.getLevel())));
        assertThat(membership2.getGuild().getId(), is(equalTo(membership.getGuild().getId())));
        assertThat(membership2.getUser().getId(), is(equalTo(membership.getUser().getId())));
    }

    @Test
    public void testCreateWithRelationshipByExternalId() {
        Guild guild = createGuild();
        String userId = getCurrentUserId();
        UserBrief user = getRecordAccessor().get(userId, UserBrief.class);
        UserBrief userByUsername = new UserBrief();
        userByUsername.setUsername(user.getUsername());

        GuildMembership membership = new GuildMembership();
        membership.setGuild(guild);
        membership.setUser(userByUsername);
        membership.setLevel("Apprentice");
        String id = getRecordAccessor().create(membership);
        membership.setId(id);

        GuildMembership membership2 = getRecordAccessor().get(membership.getId(), GuildMembership.class);
        assertThat(membership2.getId(), is(equalTo(membership.getId())));
        assertThat(membership2.getLevel(), is(equalTo(membership.getLevel())));
        assertThat(membership2.getGuild().getId(), is(equalTo(membership.getGuild().getId())));
        assertThat(membership2.getUser().getId(), is(equalTo(user.getId())));
    }

    @Test
    public void testPatch() {
        Guild guild = createGuild();

        guild.setName("Ultimate Speed Cyclists");
        getRecordAccessor().patch(guild.getId(), guild);

        Guild guild2 = new Guild();
        guild2.setDescription("A guild for really fast bicycle racers.");
        getRecordAccessor().patch(guild.getId(), guild2);

        Guild guild3 = getRecordAccessor().get(guild.getId(), Guild.class);
        assertThat(guild3.getId(), is(equalTo(guild.getId())));
        assertThat(guild3.getName(), is(equalTo(guild.getName())));
        assertThat(guild3.getDescription(), is(equalTo(guild2.getDescription())));
    }

    @Test
    public void testDelete() {
        Guild guild = createGuild();


        Guild guild2 = getRecordAccessor().get(guild.getId(), Guild.class);
        assertThat(guild2.getId(), is(equalTo(guild.getId())));

        getRecordAccessor().delete(guild.getId(), Guild.class);
        objects.remove(guild);

        try {
            getRecordAccessor().get(guild.getId(), Guild.class);
            fail("Didn't get expected RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            // This is expected
        }
    }

    @Test
    public void testDeleteNonexistentId() {
        try {
            getRecordAccessor().delete("0123456789012345", Guild.class);
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

        String shareId = getRecordAccessor().create(share);

        GuildShare share2 = getRecordAccessor().get(shareId, GuildShare.class);
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
        String feedItemId = getRecordAccessor().create(feedItem);

        FeedItem feedItem2 = getRecordAccessor().get(feedItemId, FeedItem.class);
        assertThat(feedItem2.getParent(), is(instanceOf(GuildBrief.class)));
    }

    @Test
    public void testDescribeObject() {
        ObjectMetadata objectMetadata = getRecordAccessor().describeObject("FeedItem");

        //Just checking a few values for now. Too much to check it all. Should come back and beef up someday.
        assertThat(objectMetadata, is(not(nullValue())));
        assertThat(objectMetadata.getName(), is(equalTo("FeedItem")));
        assertThat(objectMetadata.getFields().size(), is(greaterThan(0)));
    }

    private GroupBrief getAllInternalUsersGroup() {
        String soql = "select * from Group where DeveloperName=\'AllInternalUsers\'";
        List<GroupBrief> groups = getRecordAccessor().createQuery(soql, GroupBrief.class).execute();
        if (groups.size() == 0) {
            return null;
        }
        return groups.get(0);
    }

    private String getCurrentUserId() {
        AuthorizationConnector connector = getRecordAccessor().getConfig().getAuthorizationConnector();
        if (connector instanceof PasswordAuthorizationConnector) {
            return ((PasswordAuthorizationConnector) connector).getUserId();
        } else {
            throw new IllegalStateException("I don't know how to get the user id from that kind of authorization connector");
        }
    }

    private Guild createGuild() {
        String uniqueSuffix = Integer.toHexString(secureRandom.nextInt());

        Guild guild = new Guild();
        guild.setName("Speed Cyclists - " + uniqueSuffix);
        guild.setDescription("A guild for bicycle racers - " + uniqueSuffix);
        String id = getRecordAccessor().create(guild);
        guild.setId(id);
        objects.add(guild);

        return guild;
    }
}
