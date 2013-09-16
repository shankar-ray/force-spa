/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import com.force.spa.RecordOperation;
import com.force.spa.RestConnector;

public interface RestRecordOperation<T> extends RecordOperation<T> {
    void start(RestConnector connector);
}
