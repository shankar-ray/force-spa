/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import com.force.spa.metadata.ObjectMetadata;

public interface DescribeObjectOperation extends RecordOperation<ObjectMetadata> {
    String getName();
}
