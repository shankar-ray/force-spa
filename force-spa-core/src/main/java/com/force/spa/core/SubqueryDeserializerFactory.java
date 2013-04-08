/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * A special {@link DeserializerFactory} that knows how to create collection and array deserializers which can deal with
 * the Salesforce REST representation for SOQL subqueries.
 * <p/>
 * This factory is mostly a standard {@link BeanDeserializerFactory} with the exception that collection and array
 * deserializers are decorated with {@link SubqueryDeserializer} to help deal with the extra information returned with
 * SOQL subquery results.
 *
 * @see SubqueryDeserializer
 */
class SubqueryDeserializerFactory extends BeanDeserializerFactory {
    public SubqueryDeserializerFactory() {
        this(new DeserializerFactoryConfig());
    }

    public SubqueryDeserializerFactory(DeserializerFactoryConfig config) {
        super(config);
    }

    @Override
    public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
        return (getFactoryConfig() == config) ? this : new SubqueryDeserializerFactory(config);
    }

    @Override
    public JsonDeserializer<?> createArrayDeserializer(DeserializationContext ctxt, ArrayType type, BeanDescription beanDesc) throws JsonMappingException {
        return new SubqueryDeserializer(super.createArrayDeserializer(ctxt, type, beanDesc));
    }

    @Override
    public JsonDeserializer<?> createCollectionDeserializer(DeserializationContext ctxt, CollectionType type, BeanDescription beanDesc) throws JsonMappingException {
        return new SubqueryDeserializer(super.createCollectionDeserializer(ctxt, type, beanDesc));
    }

    @Override
    public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationContext ctxt, CollectionLikeType type, BeanDescription beanDesc) throws JsonMappingException {
        return new SubqueryDeserializer(super.createCollectionLikeDeserializer(ctxt, type, beanDesc));
    }
}
