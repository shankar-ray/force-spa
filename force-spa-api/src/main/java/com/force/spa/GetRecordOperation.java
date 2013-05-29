/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

public interface GetRecordOperation<T> extends RecordOperation<T> {
    String getId();

    Class<T> getRecordClass();
}
