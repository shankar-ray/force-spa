/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

public interface CreateRecordOperation<T> extends RecordOperation<String> {
    T getRecord();
}
