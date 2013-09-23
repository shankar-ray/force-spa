/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package integration.force.spa.jersey;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import com.force.spa.jersey.PasswordAuthorizationConnector;
import com.force.spa.jersey.RecordAccessorFactory;

public class AbstractRecordAccessorIntegrationTest {
    private static final SecureRandom secureRandom = new SecureRandom(RecordAccessorIntegrationTest.class.getName().getBytes());

    protected RecordAccessor accessor;
    protected final Set<Object> objects = new HashSet<Object>();

    @Before
    public void setUpRecordAccessor() {
        PasswordAuthorizationConnector authorizationConnector = new PasswordAuthorizationConnector(); //TODO
        RecordAccessorConfig config = new RecordAccessorConfig().withAuthorizationConnector(authorizationConnector);
        this.accessor = new RecordAccessorFactory(config).getRecordAccessor();
    }

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

    protected String generateUniqueNumber() {
        return Integer.toHexString(secureRandom.nextInt());
    }

    //TODO, Not here. Really don't want schema specific thing in this class.
    protected Guild createGuild() {
        String uniqueSuffix = generateUniqueNumber();

        Guild guild = new Guild();
        guild.setName("Speed Cyclists - " + uniqueSuffix);
        guild.setDescription("A guild for bicycle racers - " + uniqueSuffix);
        String id = accessor.create(guild);
        guild.setId(id);
        objects.add(guild);

        return guild;
    }
}
