/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for generating SOQL to retrieve a particular type of object. The type of object for which SOQL is desired is
 * specified in the constructor of the builder.
 * <p/>
 * This builder transforms an SOQL template by performing wildcard substitution and optionally adding pagination
 * information (in the form of LIMIT and OFFSET clauses).
 * <p/>
 * The SOQL template can contain wildcard specifications. The wildcard specifications are replaced with a concrete list
 * of fields from the associated object descriptor. Wildcard specifications can look like this:
 * <p/>
 * TODO Need a lot more description!
 */
public final class SoqlBuilder {
    private static final int DEFAULT_DEPTH = 5;
    private static final Pattern SPLIT_AT_LITERAL_PATTERN = Pattern.compile("([^\'\"]+)(.*)");
    private static final Pattern WILDCARD_PATTERN = Pattern.compile("([^\\*\\s]*?)\\.?\\*");

    private static final Map<CacheKey, String> cachedExpansions = new ConcurrentHashMap<CacheKey, String>();

    private final ObjectDescriptor rootObject;
    private String template;
    private int offset = 0;
    private int limit = 0;
    private int depth = DEFAULT_DEPTH;

    public SoqlBuilder(ObjectDescriptor rootObject) {
        this.rootObject = rootObject;
    }

    public SoqlBuilder soqlTemplate(String template) {
        Validate.notEmpty(template, "No template was specified");
        this.template = template;
        return this;
    }

    public SoqlBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    public SoqlBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public SoqlBuilder depth(int depth) {
        this.depth = depth;
        return this;
    }

    public String build() {
        StringBuilder sb = expandWildcards();
        if (limit > 0)
            sb.append(" LIMIT ").append(limit);
        if (offset > 0)
            sb.append(" OFFSET ").append(offset);
        return sb.toString();
    }

    private StringBuilder expandWildcards() {

        // Use a simple algorithm to make our job easy. Quoted literals are a headache in Regex and we really don't want
        // to bite off full SOQL parsing at this point. Simplify by assuming that all wildcard substitutions will show
        // up before any literals are encountered. Parts of the SOQL that show up after the first literal should not
        // need substitution. With this simplifying assumption we can split the line up into two parts: the front part
        // in which we perform substitutions and the back part (which contains literals) and which we leave alone.
        Matcher splitMatcher = SPLIT_AT_LITERAL_PATTERN.matcher(template);
        splitMatcher.find();
        String partToScanForWildcards = splitMatcher.group(1);
        String partToLeaveAlone = splitMatcher.group(2);

        // Replace wildcards with concrete field lists.
        int lastEnd = 0;
        StringBuilder builder = new StringBuilder();
        Matcher wildcardMatcher = WILDCARD_PATTERN.matcher(partToScanForWildcards);
        while (wildcardMatcher.find()) {
            builder.append(partToScanForWildcards.substring(lastEnd, wildcardMatcher.start()));
            String prefix = wildcardMatcher.group(1);
            builder.append(expandObject(rootObject, prefix, depth));
            lastEnd = wildcardMatcher.end();
        }
        builder.append(partToScanForWildcards.substring(lastEnd));
        builder.append(partToLeaveAlone);
        return builder;
    }

    private static String expandObject(ObjectDescriptor object, String prefix, int remainingDepth) {
        CacheKey cacheKey = new CacheKey(object, prefix);       //TODO depth matters too
        String expansion = cachedExpansions.get(cacheKey);
        if (expansion != null)
            return expansion;

        if (remainingDepth >= 0) {
            List<String> accumulator = new ArrayList<String>();
            for (FieldDescriptor field : object.getFields()) {
                if (!(object.hasAttributesField() && field.equals(object.getAttributesField()))) {
                    String expandedField = expandField(field, prefix, remainingDepth);
                    if (expandedField != null) {
                        accumulator.add(expandedField);
                    }
                }
            }
            expansion = StringUtils.join(accumulator, ",");
            cachedExpansions.put(cacheKey, expansion);
        }

        return expansion;
    }

    private static String expandField(FieldDescriptor field, String prefix, int remainingDepth) {
        if (field.isRelationship()) {
            return expandRelationshipField(field, prefix, remainingDepth);
        } else {
            return expandSimpleField(field, prefix);
        }
    }

    private static String expandSimpleField(FieldDescriptor field, String prefix) {
        return StringUtils.isEmpty(prefix) ? field.getName() : prefix + "." + field.getName();
    }

    private static String expandRelationshipField(FieldDescriptor field, String prefix, int remainingDepth) {
        if (field.isPolymorphic()) {
            return expandPolymorphicField(field, prefix, remainingDepth);
        } else if (field.isArrayOrCollection()) {
            return expandCollectionField(field, prefix, remainingDepth);
        } else {
            return expandObject(field.getRelatedObject(), expandSimpleField(field, prefix), remainingDepth - 1);
        }
    }

    private static String expandPolymorphicField(FieldDescriptor field, String prefix, int remainingDepth) {
        StringBuilder builder = new StringBuilder();
        builder.append("TYPEOF ").append(expandSimpleField(field, prefix));
        for (ObjectDescriptor object : field.getPolymorphicChoices()) {
            builder.append(" WHEN ").append(object.getName()).append(" THEN ");
            builder.append(expandObject(object, null, remainingDepth - 1));
        }
        if (field.getRelatedObject() != null) {
            builder.append(" ELSE ");
            builder.append(expandObject(field.getRelatedObject(), null, remainingDepth - 1));
        }
        builder.append(" END");

        return builder.toString();
    }

    private static String expandCollectionField(FieldDescriptor field, String prefix, int remainingDepth) {
        return new SoqlBuilder(field.getRelatedObject())
            .soqlTemplate("(SELECT * from " + expandSimpleField(field, prefix) + ")")
            .depth(remainingDepth - 1)
            .build();
    }

    private static final class CacheKey {
        private final ObjectDescriptor descriptor;
        private final String prefix;

        CacheKey(ObjectDescriptor descriptor, String prefix) {
            this.descriptor = descriptor;
            this.prefix = prefix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (descriptor != null ? !descriptor.equals(cacheKey.descriptor) : cacheKey.descriptor != null)
                return false;
            if (prefix != null ? !prefix.equals(cacheKey.prefix) : cacheKey.prefix != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = descriptor != null ? descriptor.hashCode() : 0;
            result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
            return result;
        }
    }
}
