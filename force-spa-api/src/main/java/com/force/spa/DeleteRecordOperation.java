/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

/**
 * @param <T> the type of record the operation is working with
 */
public interface DeleteRecordOperation<T> extends RecordOperation<Void> {
    String getId();

    Class<T> getRecordClass();
}
