/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("UnusedDeclaration")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicObjectMetadata {

    private boolean activateable;
    private boolean compactLayoutable;
    private boolean createable;
    private boolean custom;
    private boolean customSetting;
    private boolean deletable;
    private boolean deprecatedAndHidden;
    private boolean feedEnabled;
    private String keyPrefix;
    private String label;
    private String labelPlural;
    private boolean layoutable;
    private boolean mergeable;
    private String name;
    private boolean queryable;
    private boolean replicateable;
    private boolean retrieveable;
    private boolean searchable;
    private boolean triggerable;
    private boolean undeletable;
    private boolean updateable;

    public boolean isActivateable() {
        return activateable;
    }

    public void setActivateable(boolean activateable) {
        this.activateable = activateable;
    }

    public boolean isCompactLayoutable() {
        return compactLayoutable;
    }

    public void setCompactLayoutable(boolean compactLayoutable) {
        this.compactLayoutable = compactLayoutable;
    }

    public boolean isCreateable() {
        return createable;
    }

    public void setCreateable(boolean createable) {
        this.createable = createable;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public boolean isCustomSetting() {
        return customSetting;
    }

    public void setCustomSetting(boolean customSetting) {
        this.customSetting = customSetting;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isDeprecatedAndHidden() {
        return deprecatedAndHidden;
    }

    public void setDeprecatedAndHidden(boolean deprecatedAndHidden) {
        this.deprecatedAndHidden = deprecatedAndHidden;
    }

    public boolean isFeedEnabled() {
        return feedEnabled;
    }

    public void setFeedEnabled(boolean feedEnabled) {
        this.feedEnabled = feedEnabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelPlural() {
        return labelPlural;
    }

    public void setLabelPlural(String labelPlural) {
        this.labelPlural = labelPlural;
    }

    public boolean isLayoutable() {
        return layoutable;
    }

    public void setLayoutable(boolean layoutable) {
        this.layoutable = layoutable;
    }

    public boolean isMergeable() {
        return mergeable;
    }

    public void setMergeable(boolean mergeable) {
        this.mergeable = mergeable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isQueryable() {
        return queryable;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }

    public boolean isReplicateable() {
        return replicateable;
    }

    public void setReplicateable(boolean replicateable) {
        this.replicateable = replicateable;
    }

    public boolean isRetrieveable() {
        return retrieveable;
    }

    public void setRetrieveable(boolean retrieveable) {
        this.retrieveable = retrieveable;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isTriggerable() {
        return triggerable;
    }

    public void setTriggerable(boolean triggerable) {
        this.triggerable = triggerable;
    }

    public boolean isUndeletable() {
        return undeletable;
    }

    public void setUndeletable(boolean undeletable) {
        this.undeletable = undeletable;
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public void setUpdateable(boolean updateable) {
        this.updateable = updateable;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
