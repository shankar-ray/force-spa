/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.batch;

import com.force.spa.RecordAccessor;

import java.util.List;

public interface BatchRecordAccessor extends RecordAccessor {
    void execute(RecordOperation<?> operation);

    void execute(List<RecordOperation<?>> operations);
}
