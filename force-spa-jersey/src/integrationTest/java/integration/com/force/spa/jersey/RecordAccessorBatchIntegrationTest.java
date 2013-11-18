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

//    @Test
//    public void testMultipleCreatesAndGets() throws Exception {
//        Guild guild1 = new Guild();
//        guild1.setName("Speed Cyclists");
//        guild1.setDescription("A guild for bicycle racers.");
//        RecordOperation<String> createRecordOperation1 = getRecordAccessor().newCreateRecordOperation(guild1);
//
//        Guild guild2 = new Guild();
//        guild2.setName("<b>Speed Cyclists with some html</b>");
//        guild2.setDescription("A guild for bicycle <em>racers</em>.");
//        RecordOperation<String> createRecordOperation2 = getRecordAccessor().newCreateRecordOperation(guild2);
//
//        getRecordAccessor().execute(createRecordOperation1, createRecordOperation2);
//
//        String id1 = createRecordOperation1.get();
//        guild1.setId(id1);
//        objects.add(guild1);
//
//        String id2 = createRecordOperation2.get();
//        guild2.setId(id2);
//        objects.add(guild2);
//
//        RecordOperation<Guild> getRecordOperation1 = getRecordAccessor().newGetRecordOperation(id1, Guild.class);
//        RecordOperation<Guild> getRecordOperation2 = getRecordAccessor().newGetRecordOperation(id2, Guild.class);
//
//        getRecordAccessor().execute(getRecordOperation1, getRecordOperation2);
//
//        Guild guild3 = getRecordOperation1.get();
//        assertThat(guild3.getId(), is(equalTo(id1)));
//        assertThat(guild3.getName(), is(equalTo(guild1.getName())));
//        assertThat(guild3.getDescription(), is(equalTo(guild1.getDescription())));
//
//        Guild guild4 = getRecordOperation2.get();
//        assertThat(guild4.getId(), is(equalTo(id2)));
//        assertThat(guild4.getName(), is(equalTo(guild2.getName())));
//        assertThat(guild4.getDescription(), is(equalTo(guild2.getDescription())));
//    }
//
//    @Test
//    public void testMultiplePatches() throws Exception {
//        Guild guild = new Guild();
//        guild.setName("Speed Cyclists");
//        guild.setDescription("A guild for bicycle racers.");
//        RecordOperation<String> createRecordOperation = getRecordAccessor().newCreateRecordOperation(guild);
//        getRecordAccessor().execute(createRecordOperation);
//
//        String id = createRecordOperation.get();
//        guild.setId(id);
//        objects.add(guild);
//
//        guild.setName("Ultimate Speed Cyclists");
//        PatchRecordOperation firstPatchOperation = getRecordAccessor().newPatchRecordOperation(id, guild);
//
//        Guild guild2 = new Guild();
//        guild2.setDescription("A guild for really fast bicycle racers.");
//        PatchRecordOperation<Guild> secondPatchOperation = getRecordAccessor().newPatchRecordOperation(id, guild2);
//        getRecordAccessor().execute(firstPatchOperation, secondPatchOperation);
//
//        firstPatchOperation.get();  // Check for exceptions
//        secondPatchOperation.get(); // Check for exceptions
//
//        RecordOperation<Guild> getRecordOperation = getRecordAccessor().newGetRecordOperation(id, Guild.class);
//        getRecordAccessor().execute(getRecordOperation);
//
//        Guild guild3 = getRecordOperation.get();
//        assertThat(guild3.getId(), is(equalTo(id)));
//        assertThat(guild3.getId(), is(equalTo(id)));
//        assertThat(guild3.getName(), is(equalTo(guild.getName())));
//        assertThat(guild3.getDescription(), is(equalTo(guild2.getDescription())));
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    public void testLotsOfCreates() throws Exception {
//        int numberOfCreates = 25;
//        long before = System.currentTimeMillis();
//        List<RecordOperation<?>> createRecordOperations = new ArrayList<>();
//        for (int i = 0; i < numberOfCreates; i++) {
//            Guild guild = new Guild();
//            guild.setName("Speed Cyclists" + i);
//            guild.setDescription("A guild for bicycle racers." + i);
//            createRecordOperations.add(getRecordAccessor().newCreateRecordOperation(guild));
//        }
//        getRecordAccessor().execute(createRecordOperations);
//        long after = System.currentTimeMillis();
//        System.out.println("Elapsed create time: " + (after - before));
//
//        List<RecordOperation<?>> getRecordOperations = new ArrayList<>();
//        for (int i = 0; i < numberOfCreates; i++) {
//            String id = ((CreateRecordOperation<Guild>) createRecordOperations.get(i)).get();
//            getRecordOperations.add(getRecordAccessor().newGetRecordOperation(id, Guild.class));
//        }
//        getRecordAccessor().execute(getRecordOperations);
//
//        for (int i = 0; i < numberOfCreates; i++) {
//            Guild originalGuild = ((CreateRecordOperation<Guild>) createRecordOperations.get(i)).getRecord();
//            Guild persistentGuild = ((GetRecordOperation<Guild>) getRecordOperations.get(i)).get();
//            assertThat(originalGuild.getName(), is(equalTo(persistentGuild.getName())));
//            assertThat(originalGuild.getDescription(), is(equalTo(persistentGuild.getDescription())));
//        }
//    }
//
//    @Test
//    @Ignore("Describe not supported in a batch yet")
//    public void testMultipleDescribeObjects() throws Exception {
//        RecordOperation<ObjectMetadata> describeFeedItemOperation = getRecordAccessor().newDescribeObjectOperation("FeedItem");
//        RecordOperation<ObjectMetadata> describeFeedCommentOperation = getRecordAccessor().newDescribeObjectOperation("FeedComment");
//        RecordOperation<ObjectMetadata> describeGroupOperation = getRecordAccessor().newDescribeObjectOperation("Group");
//        RecordOperation<ObjectMetadata> describeUserOperation = getRecordAccessor().newDescribeObjectOperation("User");
//
//        getRecordAccessor().execute(describeFeedItemOperation, describeFeedCommentOperation, describeGroupOperation, describeUserOperation);
//
//        assertThat(describeFeedItemOperation.get(), is(not(nullValue())));
//        assertThat(describeFeedCommentOperation.get(), is(not(nullValue())));
//        assertThat(describeGroupOperation.get(), is(not(nullValue())));
//        assertThat(describeUserOperation.get(), is(not(nullValue())));
//    }
}
