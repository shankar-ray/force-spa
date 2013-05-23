/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.record;

import com.force.spa.SalesforceField;
import com.force.spa.SalesforceObject;

import java.net.URI;

@SalesforceObject
public class GraphicRecord extends NamedRecord {
    @SalesforceField(name = "ImageUrl")
    private URI imageUrl;

    public URI getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URI imageUrl) {
        this.imageUrl = imageUrl;
    }
}
