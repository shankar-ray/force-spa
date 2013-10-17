/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CountingJsonParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String resourcePrefix = this.getClass().getPackage().getName().replace('.', '/');

    @Test
    public void testCountForObjectWithNoParsing() throws Exception {
        JsonParser parser = objectMapper.getFactory().createParser(getResourceStream("object.json"));
        CountingJsonParser countingParser = new CountingJsonParser(parser);

        assertThat(countingParser.getCount(), is(equalTo(0L)));
    }

    @Test
    public void testCountForObjectAfterParsing() throws Exception {
        JsonParser parser = objectMapper.getFactory().createParser(getResourceStream("object.json"));
        CountingJsonParser countingParser = new CountingJsonParser(parser);

        countingParser.readValueAsTree();

        assertThat(countingParser.getCount(), is(equalTo(21709L)));
    }

    @Test
    public void testCountForArrayAfterParsing() throws Exception {
        JsonParser parser = objectMapper.getFactory().createParser(getResourceStream("array.json"));
        CountingJsonParser countingParser = new CountingJsonParser(parser);

        countingParser.readValueAsTree();

        assertThat(countingParser.getCount(), is(equalTo(452L)));
    }

    @Test
    @Ignore("Need to pursue a Jackson fix")
    public void testCountForArrayAfterParsingOneBeyond() throws Exception {
        JsonParser parser = objectMapper.getFactory().createParser(getResourceStream("array.json"));
        CountingJsonParser countingParser = new CountingJsonParser(parser);

        countingParser.readValueAsTree();

        // Try to go beyond (tickles a bug in Jackson)
        JsonToken token = countingParser.nextToken();  // There is no next token
        assertThat(token, is(nullValue()));

        assertThat(countingParser.getCount(), is(equalTo(452L)));
    }

    @Test
    public void testCountForArrayInPieces() throws Exception {
        JsonParser parser = objectMapper.getFactory().createParser(getResourceStream("array.json"));
        CountingJsonParser countingParser = new CountingJsonParser(parser);

        JsonLocation location1 = countingParser.getCurrentLocation();
        assertThat(countingParser.getCount(), is(equalTo(0L)));

        assertThat(parser.nextToken(), is(equalTo(JsonToken.START_ARRAY)));
        JsonLocation location2 = countingParser.getCurrentLocation();
        assertThat(countingParser.getCount(), is(equalTo(1L)));
        assertThat(CountingJsonParser.differenceBetween(location1, location2), is(equalTo(1L)));

        assertThat(parser.nextToken(), is(equalTo(JsonToken.START_OBJECT)));
        JsonLocation location3 = countingParser.getCurrentLocation();
        assertThat(countingParser.getCount(), is(equalTo(7L)));
        assertThat(CountingJsonParser.differenceBetween(location2, location3), is(equalTo(6L)));

        assertThat(parser.readValueAsTree(), is(instanceOf(ObjectNode.class)));
        JsonLocation location4 = countingParser.getCurrentLocation();
        assertThat(countingParser.getCount(), is(equalTo(226L)));
        assertThat(CountingJsonParser.differenceBetween(location3, location4), is(equalTo(219L)));

        assertThat(parser.readValueAsTree(), is(instanceOf(ObjectNode.class)));
        JsonLocation location5 = countingParser.getCurrentLocation();
        assertThat(countingParser.getCount(), is(equalTo(450L)));
        assertThat(CountingJsonParser.differenceBetween(location4, location5), is(equalTo(224L)));

        assertThat(parser.nextToken(), is(equalTo(JsonToken.END_ARRAY)));
        JsonLocation location6 = countingParser.getCurrentLocation();
        assertThat(countingParser.getCount(), is(equalTo(452L)));
        assertThat(CountingJsonParser.differenceBetween(location5, location6), is(equalTo(2L)));
    }

    private InputStream getResourceStream(String relativeResourceName) throws FileNotFoundException {
        return ResourceUtils.getResourceStream(resourcePrefix + '/' + relativeResourceName);
    }
}
