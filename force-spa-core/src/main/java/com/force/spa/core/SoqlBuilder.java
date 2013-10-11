/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

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
 * TODO Need more documentation here
 */
public final class SoqlBuilder {
    private static final int DEFAULT_DEPTH = 5;

    private static final Map<CacheKey, String> sharedExpansionsCache = new ConcurrentHashMap<>();

    private final AbstractRecordAccessor accessor;
    private ObjectDescriptor object;
    private String template;
    private int offset = 0;
    private int limit = 0;
    private int depth = DEFAULT_DEPTH;

    public SoqlBuilder(AbstractRecordAccessor accessor) {
        this.accessor = accessor;
    }

    public SoqlBuilder template(String template) {
        this.template = template;
        return this;
    }

    public SoqlBuilder object(Class<?> recordClass) {
        Validate.notNull(recordClass, "No recordClass was specified");

        return object(accessor.getMappingContext().getObjectDescriptor(recordClass));
    }

    public SoqlBuilder object(ObjectDescriptor object) {
        this.object = object;
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
        Validate.notNull(object, "No object was specified");
        Validate.notEmpty(template, "No template was specified");

        StringBuilder sb = expandWildcards();
        if (limit > 0)
            sb.append(" LIMIT ").append(limit);
        if (offset > 0)
            sb.append(" OFFSET ").append(offset);
        return sb.toString();
    }

    @SuppressWarnings("ConstantConditions")
    private StringBuilder expandWildcards() {

        char[] chars = template.toCharArray();
        StringBuilder builder = new StringBuilder(512);

        char quoteChar = 0;
        int pendingCursor = 0;  // Scanned characters waiting to be transferred to builder
        for (int i = 0, limit = chars.length; i < limit; i++) {
            char c = chars[i];
            if (c == '\\') {
                i++; // Skip over the next character
            } else if (quoteChar != 0) {
                if (c == quoteChar) {
                    quoteChar = 0;  // No longer inside of a quote
                }
            } else if (c == '*') {
                int prefixCursor = findPrefixStart(chars, i);
                builder.append(chars, pendingCursor, (prefixCursor - pendingCursor)); // Append pending chars up to start of prefix
                pendingCursor = i + 1;

                String prefix = (prefixCursor != i) ? new String(chars, prefixCursor, i - prefixCursor - 1) : null;
                builder.append(expandObject(object, prefix, depth));
            } else if (c == '\'' || c == '\"') {
                quoteChar = c;
            }
        }
        builder.append(chars, pendingCursor, (chars.length - pendingCursor)); // Append the rest of the chars

        return builder;
    }

    private static int findPrefixStart(final char[] chars, final int wildcardCursor) {
        if ((wildcardCursor > 0) && (chars[wildcardCursor - 1] == '.')) {
            int prefixCursor = wildcardCursor - 1;
            while (prefixCursor > 1 && chars[prefixCursor - 1] != ' ')
                prefixCursor -= 1;

            return prefixCursor;
        }
        return wildcardCursor;
    }

    private String expandObject(ObjectDescriptor object, String prefix, int remainingDepth) {
        CacheKey cacheKey = new CacheKey(object, prefix, remainingDepth);
        String expansion = sharedExpansionsCache.get(cacheKey);
        if (expansion != null)
            return expansion;

        if (remainingDepth >= 0) {
            List<String> accumulator = new ArrayList<>();
            for (FieldDescriptor field : object.getFields()) {
                if (isFieldVisible(object, field)) {
                    String expandedField = expandField(field, prefix, remainingDepth);
                    if (expandedField != null) {
                        accumulator.add(expandedField);
                    }
                }
            }
            expansion = StringUtils.join(accumulator, ",");
            if (isCacheable(object)) {
                sharedExpansionsCache.put(cacheKey, expansion);
            }
        }
        return expansion;
    }

    private static boolean isFieldVisible(ObjectDescriptor object, FieldDescriptor field) {
        if (isAttributesField(object, field)) {
            return false;
        } else if (object.isMetadataAware()) {
            throw new UnsupportedOperationException("Not implemented yet"); //TODO Use metadata to determine field visibility
        } else {
            return true;
        }
    }

    private static boolean isAttributesField(ObjectDescriptor object, FieldDescriptor field) {
        return object.hasAttributesField() && field.equals(object.getAttributesField());
    }

    private static boolean isCacheable(ObjectDescriptor object) {
        return !object.isMetadataAware(); // Leveraging per-user metadata makes it useless in a shared cache
    }

    private String expandField(FieldDescriptor field, String prefix, int remainingDepth) {
        if (field.isRelationship()) {
            return expandRelationshipField(field, prefix, remainingDepth);
        } else {
            return expandSimpleField(field, prefix);
        }
    }

    private static String expandSimpleField(FieldDescriptor field, String prefix) {
        return StringUtils.isEmpty(prefix) ? field.getName() : prefix + "." + field.getName();
    }

    private String expandRelationshipField(FieldDescriptor field, String prefix, int remainingDepth) {
        if (field.isPolymorphic()) {
            return expandPolymorphicField(field, prefix, remainingDepth);
        } else if (field.getJavaType().isContainerType()) {
            return expandContainerField(field, prefix, remainingDepth);
        } else {
            return expandObject(field.getRelatedObject(), expandSimpleField(field, prefix), remainingDepth - 1);
        }
    }

    private String expandPolymorphicField(FieldDescriptor field, String prefix, int remainingDepth) {
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

    private String expandContainerField(FieldDescriptor field, String prefix, int remainingDepth) {
        if (StringUtils.isEmpty(prefix)) {
            return new SoqlBuilder(accessor)
                .object(field.getRelatedObject())
                .template("(SELECT * from " + expandSimpleField(field, prefix) + ")")
                .depth(remainingDepth - 1)
                .build();
        } else {
            // The server can't handle this kind of nesting. The server complains with "First SObject of a nested query
            // must be a child of its outer query". We can only query down into collection fields if the container
            // is at the top of the query. If the container is nested down inside somewhere then we have to skip the
            // contained objects.
            return null;
        }
    }

    private static final class CacheKey {
        private final ObjectDescriptor descriptor;
        private final String prefix;
        private final int remainingDepth;

        CacheKey(ObjectDescriptor descriptor, String prefix, int remainingDepth) {
            this.descriptor = descriptor;
            this.prefix = prefix;
            this.remainingDepth = remainingDepth;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (remainingDepth != cacheKey.remainingDepth) return false;
            if (!descriptor.equals(cacheKey.descriptor)) return false;
            if (prefix != null ? !prefix.equals(cacheKey.prefix) : cacheKey.prefix != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = descriptor.hashCode();
            result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
            result = 31 * result + remainingDepth;
            return result;
        }
    }
}
