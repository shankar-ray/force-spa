/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package integration.com.force.spa.jersey;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.force.spa.CreateRecordOperation;
import com.force.spa.GetRecordOperation;
import com.force.spa.PatchRecordOperation;
import com.force.spa.RecordOperation;
import com.force.spa.metadata.ObjectMetadata;

public class RecordAccessorBatchIntegrationTest extends AbstractRecordAccessorIntegrationTest {

    private static final SecureRandom secureRandom = new SecureRandom();

    @Test
    @Ignore("Batching not available in production yet")
    public void testMultipleCreatesAndGets() throws Exception {
        String uniqueSuffix = Integer.toHexString(secureRandom.nextInt());

        Account account1 = new Account();
        account1.setName("Big Account - " + uniqueSuffix);
        account1.setAnnualRevenue(secureRandom.nextDouble() * 1000000D);
        RecordOperation<String> createRecordOperation1 = getRecordAccessor().newCreateRecordOperation(account1);

        Account account2 = new Account();
        account2.setName("Small Account - " + uniqueSuffix);
        account2.setAnnualRevenue(secureRandom.nextDouble() * 1000D);
        RecordOperation<String> createRecordOperation2 = getRecordAccessor().newCreateRecordOperation(account2);

        getRecordAccessor().execute(createRecordOperation1, createRecordOperation2);

        String id1 = createRecordOperation1.get();
        account1.setId(id1);
        objects.add(account1);

        String id2 = createRecordOperation2.get();
        account2.setId(id2);
        objects.add(account2);

        RecordOperation<Account> getRecordOperation1 = getRecordAccessor().newGetRecordOperation(id1, Account.class);
        RecordOperation<Account> getRecordOperation2 = getRecordAccessor().newGetRecordOperation(id2, Account.class);

        getRecordAccessor().execute(getRecordOperation1, getRecordOperation2);

        Account account1Prime = getRecordOperation1.get();
        assertThat(account1Prime.getId(), is(equalTo(id1)));
        assertThat(account1Prime.getName(), is(equalTo(account1.getName())));
        assertThat(account1Prime.getAnnualRevenue(), is(equalTo(account1.getAnnualRevenue())));

        Account account2Prime = getRecordOperation2.get();
        assertThat(account2Prime.getId(), is(equalTo(id2)));
        assertThat(account2Prime.getName(), is(equalTo(account2.getName())));
        assertThat(account2Prime.getAnnualRevenue(), is(equalTo(account2.getAnnualRevenue())));
    }

    @Test
    @Ignore("Batching not available in production yet")
    public void testMultiplePatches() throws Exception {
        String uniqueSuffix = Integer.toHexString(secureRandom.nextInt());

        Account account = new Account();
        account.setName("Big Account - " + uniqueSuffix);
        account.setAnnualRevenue(secureRandom.nextDouble() * 1000000D);
        RecordOperation<String> createRecordOperation = getRecordAccessor().newCreateRecordOperation(account);
        getRecordAccessor().execute(createRecordOperation);

        String id = createRecordOperation.get();
        account.setId(id);
        objects.add(account);

        account.setName("Even Bigger Account - " + uniqueSuffix);
        PatchRecordOperation firstPatchOperation = getRecordAccessor().newPatchRecordOperation(id, account);

        Account account2 = new Account();
        account2.setName("Biggest Account - " + uniqueSuffix);
        PatchRecordOperation<Account> secondPatchOperation = getRecordAccessor().newPatchRecordOperation(id, account2);
        getRecordAccessor().execute(firstPatchOperation, secondPatchOperation);

        firstPatchOperation.get();  // Check for exceptions
        secondPatchOperation.get(); // Check for exceptions

        RecordOperation<Account> getRecordOperation = getRecordAccessor().newGetRecordOperation(id, Account.class);
        getRecordAccessor().execute(getRecordOperation);

        Account account3 = getRecordOperation.get();
        assertThat(account3.getId(), is(equalTo(id)));
        assertThat(account3.getId(), is(equalTo(id)));
        assertThat(account3.getName(), is(equalTo(account.getName())));
        assertThat(account3.getAnnualRevenue(), is(equalTo(account2.getAnnualRevenue())));
    }

    @Test
    @SuppressWarnings("unchecked")
    @Ignore("Batching not available in production yet")
    public void testLotsOfCreates() throws Exception {
        String uniqueSuffix = Integer.toHexString(secureRandom.nextInt());

        int numberOfCreates = 25;
        long before = System.currentTimeMillis();
        List<RecordOperation<?>> createRecordOperations = new ArrayList<>();
        for (int i = 0; i < numberOfCreates; i++) {
            Account account = new Account();
            account.setName("Big Account " + i + " - " + uniqueSuffix);
            account.setAnnualRevenue(secureRandom.nextDouble() * 1000000D);
            createRecordOperations.add(getRecordAccessor().newCreateRecordOperation(account));
        }
        getRecordAccessor().execute(createRecordOperations);
        long after = System.currentTimeMillis();
        System.out.println("Elapsed create time: " + (after - before));

        List<RecordOperation<?>> getRecordOperations = new ArrayList<>();
        for (int i = 0; i < numberOfCreates; i++) {
            String id = ((CreateRecordOperation<Account>) createRecordOperations.get(i)).get();
            getRecordOperations.add(getRecordAccessor().newGetRecordOperation(id, Account.class));
        }
        getRecordAccessor().execute(getRecordOperations);

        for (int i = 0; i < numberOfCreates; i++) {
            Account originalAccount = ((CreateRecordOperation<Account>) createRecordOperations.get(i)).getRecord();
            Account persistentAccount = ((GetRecordOperation<Account>) getRecordOperations.get(i)).get();
            assertThat(originalAccount.getName(), is(equalTo(persistentAccount.getName())));
            assertThat(originalAccount.getAnnualRevenue(), is(equalTo(persistentAccount.getAnnualRevenue())));
        }
    }

    @Test
    @Ignore("Batching not available in production yet")
    public void testMultipleDescribeObjects() throws Exception {
        RecordOperation<ObjectMetadata> describeFeedItemOperation = getRecordAccessor().newDescribeObjectOperation("FeedItem");
        RecordOperation<ObjectMetadata> describeFeedCommentOperation = getRecordAccessor().newDescribeObjectOperation("FeedComment");
        RecordOperation<ObjectMetadata> describeGroupOperation = getRecordAccessor().newDescribeObjectOperation("Group");
        RecordOperation<ObjectMetadata> describeUserOperation = getRecordAccessor().newDescribeObjectOperation("User");

        getRecordAccessor().execute(describeFeedItemOperation, describeFeedCommentOperation, describeGroupOperation, describeUserOperation);

        assertThat(describeFeedItemOperation.get(), is(not(nullValue())));
        assertThat(describeFeedCommentOperation.get(), is(not(nullValue())));
        assertThat(describeGroupOperation.get(), is(not(nullValue())));
        assertThat(describeUserOperation.get(), is(not(nullValue())));
    }
}
