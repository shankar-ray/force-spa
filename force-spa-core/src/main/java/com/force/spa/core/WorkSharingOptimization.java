/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.HashMap;
import java.util.Map;

/**
 * A special (hack) optimization to make creation of complex sharing rows efficient with Work.com objects. Maybe in the
 * future we can get something better and more official from Salesforce core.
 * <p/>
 * A string specifying a (possibly large) set of sharing rows is passed so the Salesforce server along with object
 * creation or modification. When the Salesforce server-side code sees the sharing specification all the requested
 * sharing rows are created at the same time as the object creation/modification and inside of the same transaction.
 * <p/>
 * The sharing specification is conveyed by the application code to this library inside of the "attributes" member of
 * the bean. It is obscure, but effective. Alternative hacks end up being less convenient or more obtrusive.
 * <p/>
 * The information is passed to the Salesforce server as an HTTP header along with the REST request.
 * <p/>
 * This class transfers the information (if present) from the bean to the headers.
 */
final class WorkSharingOptimization {

    static final String SHARING_SPECIFICATION_HEADER_NAME = "Work-Sharing-Specification";
    static final String SHARING_SPECIFICATION_ATTRIBUTE_NAME = "sharingSpecification";

    private WorkSharingOptimization() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    static Map<String, String> updateHeaders(ObjectDescriptor descriptor, Object record, Map<String, String> headers) {
        if (record != null && descriptor.hasAttributesMember()) {
            Map<String, String> attributes = RecordUtils.getAttributes(descriptor, record);
            if (attributes != null) {
                String sharingSpecification = attributes.get(SHARING_SPECIFICATION_ATTRIBUTE_NAME);
                if (sharingSpecification != null) {
                    headers = (headers == null) ? new HashMap<String, String>() : headers;
                    headers.put(SHARING_SPECIFICATION_HEADER_NAME, sharingSpecification);
                }
            }
        }
        return headers;
    }
}
