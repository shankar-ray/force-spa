/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import com.force.spa.RecordAccessor;
import com.force.spa.RecordAccessorConfig;
import org.junit.After;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public class AbstractRecordAccessorIntegrationTest {
    private static SecureRandom secureRandom = new SecureRandom(RecordAccessorIntegrationTest.class.getName().getBytes());

    protected PasswordAuthorizationConnector authorizationConnector = new PasswordAuthorizationConnector();
    protected RecordAccessor accessor = new RecordAccessorFactory().newInstance(new RecordAccessorConfig(), authorizationConnector);
    protected Set<Object> objects = new HashSet<Object>();

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

    protected Guild createTestGuild() {
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
