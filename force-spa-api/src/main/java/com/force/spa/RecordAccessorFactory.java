/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

/**
 * A factory that returns fully configured instances of {@link RecordAccessor}.
 */
public interface RecordAccessorFactory {
    /**
     * Creates a new fully configured instance of {@link RecordAccessor} using values configured for the factory.
     *
     * @return the rest connector
     */
    RecordAccessor getRecordAccessor();
}
