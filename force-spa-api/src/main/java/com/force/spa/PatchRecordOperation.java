/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

public interface PatchRecordOperation extends RecordOperation<Void> {
    String getId();

    Object getRecord();
}
