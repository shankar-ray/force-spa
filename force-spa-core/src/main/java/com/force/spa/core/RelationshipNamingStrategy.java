/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;

import static com.force.spa.core.IntrospectionUtils.isChildToParentRelationship;
import static com.force.spa.core.IntrospectionUtils.isPropertyOfCustomEntity;
import static com.force.spa.core.IntrospectionUtils.isStandardProperty;

/**
 * A property naming strategy which helps with the subtleties of relationship field naming.
 * <p/>
 * The relationship field names in Salesforce differ depending on whether you want to address the "id" of the related
 * object or you want to address other fields of the related object (through its relationship name). Additionally, the
 * field names for the id and relationship differ depending on whether it is a custom field or a standard field.
 */
final class RelationshipNamingStrategy extends PropertyNamingStrategy {

    private boolean useRelationshipNameForm;

    RelationshipNamingStrategy(boolean useRelationshipNameForm) {
        this.useRelationshipNameForm = useRelationshipNameForm;
    }

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return translate(field, defaultName);
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translate(method, defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translate(method, defaultName);
    }

    @Override
    public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, String defaultName) {
        return translate(ctorParam, defaultName);
    }

    protected String translate(AnnotatedMember member, String propertyName) {
        if (isChildToParentRelationship(member)) {
            if (isCustomProperty(member, propertyName)) {
                propertyName = translateCustom(member, propertyName);
            } else {
                propertyName = translateStandard(member, propertyName);
            }
        }
        return propertyName;
    }

    private String translateCustom(AnnotatedMember member, String propertyName) {
        String propertyNameSansSuffix = propertyName;
        if (isCustomSuffixPresent(propertyName))
            propertyNameSansSuffix = propertyName.substring(0, propertyName.length() - 3);

        return propertyNameSansSuffix + (useRelationshipNameForm ? "__r" : "__c");
    }

    private String translateStandard(AnnotatedMember member, String propertyName) {
        String propertyNameSansId = propertyName;
        if (propertyName.endsWith("Id"))
            propertyNameSansId = propertyName.substring(0, propertyName.length() - 2);

        return useRelationshipNameForm ? propertyNameSansId : (propertyNameSansId + "Id");
    }

    private boolean isCustomProperty(AnnotatedMember member, String propertyName) {
        return isCustomSuffixPresent(propertyName) || (isPropertyOfCustomEntity(member) && !isStandardProperty(propertyName));
    }

    private boolean isCustomSuffixPresent(String propertyName) {
        return propertyName.endsWith("__c") || propertyName.endsWith("__r");
    }
}
