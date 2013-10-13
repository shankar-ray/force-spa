/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

/**
 * @param <T> the type of record the operation is working with
 */
public interface PatchRecordOperation<T> extends Operation<Void> {
    String getId();

    T getRecord();
}
