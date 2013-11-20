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

import org.junit.Ignore;
import org.junit.Test;

import com.force.spa.RecordNotFoundException;
import com.force.spa.beans.Record;
import com.force.spa.metadata.ObjectMetadata;

public class RecordAccessorIntegrationTest extends AbstractRecordAccessorIntegrationTest {

    private static final SecureRandom secureRandom = new SecureRandom();

    @Test
    public void testCreateAccountWithNote() {
        Account account = createAccount();
        Note note = createNote(account);

        Account account2 = getRecordAccessor().get(account.getId(), Account.class);
        assertThat(account2.getId(), is(equalTo(account.getId())));
        assertThat(account2.getName(), is(equalTo(account.getName())));
        assertThat(account2.getAnnualRevenue(), is(equalTo(account.getAnnualRevenue())));

        assertThat(account2.getNotes().size(), is(equalTo(1)));
        assertThat(account2.getNotes().get(0).getTitle(), is(equalTo(note.getTitle())));
        assertThat(account2.getNotes().get(0).getBody(), is(equalTo(note.getBody())));
        assertThat(account2.getNotes().get(0).getParent().getId(), is(equalTo(account.getId())));
    }

    @Test
    @Ignore("Exposes bug (or lack of feature) for base typed parent field")
    public void testCreateAccountWithNoteByExternalId() {
        Account account = createAccount();

        Account accountWithJustName = new Account();
        accountWithJustName.setName(account.getName());
        Note note = createNote(accountWithJustName);

        Account account2 = getRecordAccessor().get(account.getId(), Account.class);
        assertThat(account2.getId(), is(equalTo(account.getId())));
        assertThat(account2.getName(), is(equalTo(account.getName())));
        assertThat(account2.getAnnualRevenue(), is(equalTo(account.getAnnualRevenue())));

        assertThat(account2.getNotes().size(), is(equalTo(1)));
        assertThat(account2.getNotes().get(0).getTitle(), is(equalTo(note.getTitle())));
        assertThat(account2.getNotes().get(0).getBody(), is(equalTo(note.getBody())));
        assertThat(account2.getNotes().get(0).getParent().getId(), is(equalTo(account.getId())));
    }

    @Test
    public void testUpdate() {
        Account account = createAccount();
        account = getRecordAccessor().get(account.getId(), Account.class);

        String newName = "Special " + account.getName();
        account.setName(newName);
        getRecordAccessor().update(account.getId(), account);

        Account account2 = getRecordAccessor().get(account.getId(), Account.class);
        assertThat(account2.getId(), is(equalTo(account.getId())));
        assertThat(account2.getName(), is(equalTo(newName)));
        assertThat(account2.getAnnualRevenue(), is(equalTo(account.getAnnualRevenue())));
    }

    @Test
    public void testPatch() {
        Account account = createAccount();

        String newName = "Special " + account.getName();
        Account accountChanges1 = new Account();
        accountChanges1.setName(newName);
        getRecordAccessor().patch(account.getId(), accountChanges1);

        Double newAnnualRevenue = account.getAnnualRevenue() * 2;
        Account accountChanges2 = new Account();
        accountChanges2.setAnnualRevenue(newAnnualRevenue);
        getRecordAccessor().patch(account.getId(), accountChanges2);

        Account account3 = getRecordAccessor().get(account.getId(), Account.class);
        assertThat(account3.getId(), is(equalTo(account.getId())));
        assertThat(account3.getName(), is(equalTo(newName)));
        assertThat(account3.getAnnualRevenue(), is(equalTo(newAnnualRevenue)));
    }

    @Test
    public void testDelete() {
        Account account = createAccount();

        getRecordAccessor().delete(account.getId(), Account.class);
        objects.remove(account);

        try {
            getRecordAccessor().get(account.getId(), Account.class);
            fail("Didn't get expected RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            // This is expected
        }
    }

    @Test
    public void testDeleteNonexistentId() {
        try {
            getRecordAccessor().delete("0123456789012345", Account.class);
            fail("Didn't get expected RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            // This is expected
        }
    }

    @Test
    public void testFeedItemPolymorphism() {
        Account account = createAccount();

        FeedItem feedItem = new FeedItem();
        feedItem.setParent(account);
        feedItem.setBody("Feed item body");
        String feedItemId = getRecordAccessor().create(feedItem);

        FeedItem feedItem2 = getRecordAccessor().get(feedItemId, FeedItem.class);
        assertThat(feedItem2.getParent(), is(instanceOf(AccountBrief.class)));
    }

    @Test
    public void testDescribeObject() {
        ObjectMetadata objectMetadata = getRecordAccessor().describeObject("FeedItem");

        //Just checking a few values for now. Too much to check it all. Should come back and beef up someday.
        assertThat(objectMetadata, is(not(nullValue())));
        assertThat(objectMetadata.getName(), is(equalTo("FeedItem")));
        assertThat(objectMetadata.getFields().size(), is(greaterThan(0)));
    }

    private Account createAccount() {
        String uniqueSuffix = Integer.toHexString(secureRandom.nextInt());

        Account account = new Account();
        account.setName("Big Account - " + uniqueSuffix);
        account.setAnnualRevenue(secureRandom.nextDouble() * 1000000D);
        String id = getRecordAccessor().create(account);
        account.setId(id);
        objects.add(account);

        return account;
    }

    private Note createNote(Record parent) {
        String uniqueSuffix = Integer.toHexString(secureRandom.nextInt());

        Note note = new Note();
        note.setTitle("Title - " + uniqueSuffix);
        note.setBody("Body - " + uniqueSuffix);
        note.setParent(parent);
        String id = getRecordAccessor().create(note);
        note.setId(id);

        return note;
    }
}
