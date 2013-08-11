/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

/**
 * View definitions for controlling Jackson serialization.
 */
class SerializationViews {
    /**
     * View that is specified when serializing an object for {@link com.force.spa.RecordAccessor#create(Object)}.
     */
    static class Create {
    }

    /**
     * View that is specified when serializing an object for {@link com.force.spa.RecordAccessor#update(Object)}}.
     */
    static class Update {
    }

    /**
     * View that is specified when serializing an object for {@link com.force.spa.RecordAccessor#patch(String,Object)}}.
     */
    static class Patch {
    }

    /**
     * View that is never specified at serialization time so any field marked with this view will never be serialized.
     */
    static class Never {
    }
}
