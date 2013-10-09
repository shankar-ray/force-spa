/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

/**
 * A special version of {@link BeanSerializerFactory} that is aware of relationships and uses this information to build
 * a special {@link RelationshipBeanPropertyWriter} instance that can adaptively serialize the relationship correctly
 * based on the content of the related object.
 */
final class RelationshipAwareBeanSerializerFactory extends BeanSerializerFactory {
    private static final long serialVersionUID = 91029020789141671L;

    private final MappingContext mappingContext;

    RelationshipAwareBeanSerializerFactory(MappingContext mappingContext) {
        this(mappingContext, null);
    }

    private RelationshipAwareBeanSerializerFactory(MappingContext mappingContext, SerializerFactoryConfig config) {
        super(config);
        this.mappingContext = mappingContext;
    }

    @Override
    public SerializerFactory withConfig(SerializerFactoryConfig config) {
        if (_factoryConfig == config) {
            return this;
        }

        return new RelationshipAwareBeanSerializerFactory(this.mappingContext, config);
    }

    @Override
    protected List<BeanPropertyWriter> findBeanProperties(SerializerProvider prov, BeanDescription beanDesc, BeanSerializerBuilder builder) throws JsonMappingException {
        List<BeanPropertyWriter> originalWriters = super.findBeanProperties(prov, beanDesc, builder);
        if (originalWriters != null) {
            ObjectDescriptor object = mappingContext.getObjectDescriptor(beanDesc.getBeanClass());
            List<BeanPropertyWriter> updatedWriters = new ArrayList<>();
            for (BeanPropertyWriter originalWriter : originalWriters) {
                FieldDescriptor field = object.getField(originalWriter.getName());
                if (field.isRelationship()) {
                    updatedWriters.add(new RelationshipBeanPropertyWriter(originalWriter, mappingContext));
                } else {
                    updatedWriters.add(originalWriter);
                }
            }
            return updatedWriters;
        } else {
            return originalWriters;
        }
    }
}
