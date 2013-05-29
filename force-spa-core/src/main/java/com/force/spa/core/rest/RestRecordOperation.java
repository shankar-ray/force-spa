/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import com.force.spa.RestConnector;
import com.force.spa.RecordOperation;
import com.force.spa.core.ObjectMappingContext;

public interface RestRecordOperation<T> extends RecordOperation<T> {
    void start(RestConnector connector, ObjectMappingContext mappingContext);
}
