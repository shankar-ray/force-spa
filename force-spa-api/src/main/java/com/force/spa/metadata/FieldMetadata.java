/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.metadata;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("UnusedDeclaration")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldMetadata {

    private boolean autoNumber;
    private int byteLength;
    private boolean calculated;
    private String calculatedFormula;
    private boolean cascadeDelete;
    private boolean caseSensitive;
    private String controllerName;
    private boolean createable;
    private boolean custom;
    private String defaultValue;
    private String defaultValueFormula;
    private boolean defaultedOnCreate;
    private boolean dependentPicklist;
    private boolean deprecatedAndHidden;
    private int digits;
    private boolean displayLocationInDecimal;
    private boolean externalId;
    private boolean filterable;
    private boolean groupable;
    private boolean htmlFormatted;
    private boolean idLookup;
    private String inlineHelpText;
    private String label;
    private int length;
    private String name;
    private boolean nameField;
    private boolean namePointing;
    private boolean nillable;
    private boolean permissionable;
    private List<PicklistValue> picklistValues;
    private int precision;
    private List<String> referenceTo;
    private String relationshipName;
    private String relationshipOrder;
    private boolean restrictedDelete;
    private boolean restrictedPicklist;
    private int scale;
    private String soapType;
    private boolean sortable;
    private String type;
    private boolean unique;
    private boolean updateable;
    private boolean writeRequiresMasterRead;

    public boolean isAutoNumber() {
        return autoNumber;
    }

    public void setAutoNumber(boolean autoNumber) {
        this.autoNumber = autoNumber;
    }

    public int getByteLength() {
        return byteLength;
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public String getCalculatedFormula() {
        return calculatedFormula;
    }

    public void setCalculatedFormula(String calculatedFormula) {
        this.calculatedFormula = calculatedFormula;
    }

    public boolean isCascadeDelete() {
        return cascadeDelete;
    }

    public void setCascadeDelete(boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
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

    public boolean isDefaultedOnCreate() {
        return defaultedOnCreate;
    }

    public void setDefaultedOnCreate(boolean defaultedOnCreate) {
        this.defaultedOnCreate = defaultedOnCreate;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValueFormula() {
        return defaultValueFormula;
    }

    public void setDefaultValueFormula(String defaultValueFormula) {
        this.defaultValueFormula = defaultValueFormula;
    }

    public boolean isDependentPicklist() {
        return dependentPicklist;
    }

    public void setDependentPicklist(boolean dependentPicklist) {
        this.dependentPicklist = dependentPicklist;
    }

    public boolean isDeprecatedAndHidden() {
        return deprecatedAndHidden;
    }

    public void setDeprecatedAndHidden(boolean deprecatedAndHidden) {
        this.deprecatedAndHidden = deprecatedAndHidden;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public boolean isDisplayLocationInDecimal() {
        return displayLocationInDecimal;
    }

    public void setDisplayLocationInDecimal(boolean displayLocationInDecimal) {
        this.displayLocationInDecimal = displayLocationInDecimal;
    }

    public boolean isExternalId() {
        return externalId;
    }

    public void setExternalId(boolean externalId) {
        this.externalId = externalId;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public boolean isGroupable() {
        return groupable;
    }

    public void setGroupable(boolean groupable) {
        this.groupable = groupable;
    }

    public boolean isHtmlFormatted() {
        return htmlFormatted;
    }

    public void setHtmlFormatted(boolean htmlFormatted) {
        this.htmlFormatted = htmlFormatted;
    }

    public boolean isIdLookup() {
        return idLookup;
    }

    public void setIdLookup(boolean idLookup) {
        this.idLookup = idLookup;
    }

    public String getInlineHelpText() {
        return inlineHelpText;
    }

    public void setInlineHelpText(String inlineHelpText) {
        this.inlineHelpText = inlineHelpText;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNameField() {
        return nameField;
    }

    public void setNameField(boolean nameField) {
        this.nameField = nameField;
    }

    public boolean isNamePointing() {
        return namePointing;
    }

    public void setNamePointing(boolean namePointing) {
        this.namePointing = namePointing;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public boolean isPermissionable() {
        return permissionable;
    }

    public void setPermissionable(boolean permissionable) {
        this.permissionable = permissionable;
    }

    public List<PicklistValue> getPicklistValues() {
        return picklistValues;
    }

    public void setPicklistValues(List<PicklistValue> picklistValues) {
        this.picklistValues = picklistValues;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public List<String> getReferenceTo() {
        return referenceTo;
    }

    public void setReferenceTo(List<String> referenceTo) {
        this.referenceTo = referenceTo;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    public String getRelationshipOrder() {
        return relationshipOrder;
    }

    public void setRelationshipOrder(String relationshipOrder) {
        this.relationshipOrder = relationshipOrder;
    }

    public boolean isRestrictedDelete() {
        return restrictedDelete;
    }

    public void setRestrictedDelete(boolean restrictedDelete) {
        this.restrictedDelete = restrictedDelete;
    }

    public boolean isRestrictedPicklist() {
        return restrictedPicklist;
    }

    public void setRestrictedPicklist(boolean restrictedPicklist) {
        this.restrictedPicklist = restrictedPicklist;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getSoapType() {
        return soapType;
    }

    public void setSoapType(String soapType) {
        this.soapType = soapType;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public void setUpdateable(boolean updateable) {
        this.updateable = updateable;
    }

    public boolean isWriteRequiresMasterRead() {
        return writeRequiresMasterRead;
    }

    public void setWriteRequiresMasterRead(boolean writeRequiresMasterRead) {
        this.writeRequiresMasterRead = writeRequiresMasterRead;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class PicklistValue {
        private boolean active;
        private boolean defaultValue;
        private String label;
        private String validFor;
        private String value;

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(boolean defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValidFor() {
            return validFor;
        }

        public void setValidFor(String validFor) {
            this.validFor = validFor;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
