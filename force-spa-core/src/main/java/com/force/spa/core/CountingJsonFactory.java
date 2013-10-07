/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

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
    protected JsonParser _createParser(Reader r, IOContext ctxt) throws IOException {
        return new CountingJsonParser(super._createParser(r, ctxt));
    }

    @Override
    protected JsonParser _createParser(InputStream in, IOContext ctxt) throws IOException {
        return new CountingJsonParser(super._createParser(in, ctxt));
    }

    @Override
    protected JsonParser _createParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException {
        return new CountingJsonParser(super._createParser(data, offset, len, ctxt));
    }
}
