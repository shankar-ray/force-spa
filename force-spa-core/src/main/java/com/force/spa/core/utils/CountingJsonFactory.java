/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.MappingJsonFactory;

/**
 * An extension of the standard {@link MappingJsonFactory} that returns {@link CountingJsonParser} instances that help
 * with gathering statistics.
 */
public class CountingJsonFactory extends MappingJsonFactory {
    @Override
    protected JsonParser _createParser(Reader reader, IOContext context) throws IOException {
        return new CountingJsonParser(super._createParser(reader, context));
    }

    @Override
    protected JsonParser _createParser(InputStream inputStream, IOContext context) throws IOException {
        return new CountingJsonParser(super._createParser(inputStream, context));
    }

    @Override
    protected JsonParser _createParser(byte[] data, int offset, int length, IOContext context) throws IOException {
        return new CountingJsonParser(super._createParser(data, offset, length, context));
    }
}
