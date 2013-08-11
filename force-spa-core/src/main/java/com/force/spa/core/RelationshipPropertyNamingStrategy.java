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

import static com.force.spa.core.IntrospectionUtils.canBeSalesforceObject;
import static com.force.spa.core.IntrospectionUtils.getConcreteClass;
import static com.force.spa.core.IntrospectionUtils.isStandardProperty;

/**
 * A property naming strategy which helps with the subtleties of relationship field naming.
 * <p/>
 * The relationship field names in Salesforce differ depending on whether you want to address the "id" of the related
 * object or you want to address other fields of the related object (through its relationship name). Additionally, the
 * field names for the id and relationship differ depending on whether it is a custom field or a standard field.
 * <p/>
 * This naming strategy normalizes the name to refer to the relationship.
 */
final class RelationshipPropertyNamingStrategy extends PropertyNamingStrategy {

    private static final long serialVersionUID = -2489422892963688271L;

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

    String translate(AnnotatedMember member, String name) {
        if (canBeSalesforceObject(getConcreteClass(member), false)) {
            if (isCustomProperty(member, name)) {
                return translateCustomName(name);
            } else {
                return translateStandardName(name);
            }
        }
        return name;
    }

    private String translateCustomName(String name) {
        String propertyNameSansSuffix = name;
        if (isCustomSuffixPresent(name))
            propertyNameSansSuffix = name.substring(0, name.length() - 3);

        return propertyNameSansSuffix + "__r";
    }

    private String translateStandardName(String name) {
        String propertyNameSansId = name;
        if (name.endsWith("Id"))
            propertyNameSansId = name.substring(0, name.length() - 2);

        return propertyNameSansId;
    }

    private static boolean isCustomProperty(AnnotatedMember member, String name) {
        return isCustomSuffixPresent(name) || (isPropertyOfCustomEntity(member) && !isStandardProperty(name));
    }

    private static boolean isCustomSuffixPresent(String name) {
        return name.endsWith("__c") || name.endsWith("__r");
    }

    private static boolean isPropertyOfCustomEntity(AnnotatedMember member) {
        return SpaAnnotationIntrospector.findSalesforceObjectName(member.getDeclaringClass()).endsWith("__c");
    }
}
