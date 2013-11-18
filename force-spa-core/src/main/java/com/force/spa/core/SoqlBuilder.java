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
                builder.append(expandObject(object, new Context(prefix, depth, false)));
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

    private String expandObject(ObjectDescriptor object, Context context) {
        CacheKey cacheKey = new CacheKey(object, context);
        String expansion = sharedExpansionsCache.get(cacheKey);
        if (expansion != null)
            return expansion;

        if (context.getRemainingDepth() >= 0) {
            List<String> accumulator = new ArrayList<>();
            for (FieldDescriptor field : object.getFields()) {
                if (isFieldVisible(object, field)) {
                    String expandedField = expandField(field, context);
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

    private String expandField(FieldDescriptor field, Context context) {
        if (field.isRelationship()) {
            return expandRelationshipField(field, context);
        } else {
            return expandSimpleField(field, context);
        }
    }

    private static String expandSimpleField(FieldDescriptor field, Context context) {
        return StringUtils.isEmpty(context.getPrefix()) ? field.getName() : context.getPrefix() + "." + field.getName();
    }

    private String expandRelationshipField(FieldDescriptor field, Context context) {
        if (field.isPolymorphic()) {
            return expandPolymorphicField(field, context);
        } else if (field.getJavaType().isContainerType()) {
            return expandContainerField(field, context);
        } else {
            String nestedPrefix = expandSimpleField(field, context);
            Context nestedContext = new Context(nestedPrefix, context.getRemainingDepth() - 1, context.isReferencedPolymorphically());
            return expandObject(field.getRelatedObject(), nestedContext);
        }
    }

    private String expandPolymorphicField(FieldDescriptor field, Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append("TYPEOF ").append(expandSimpleField(field, context));
        Context nestedContext = new Context(null, context.getRemainingDepth() - 1, true);
        for (ObjectDescriptor object : field.getPolymorphicChoices()) {
            builder.append(" WHEN ").append(object.getName()).append(" THEN ");
            builder.append(expandObject(object, nestedContext));
        }
        if (field.getRelatedObject() != null) {
            builder.append(" ELSE ");
            builder.append(expandObject(field.getRelatedObject(), nestedContext));
        }
        builder.append(" END");

        return builder.toString();
    }

    private String expandContainerField(FieldDescriptor field, Context context) {
        if (context.isReferencedPolymorphically()) {
            // The server can't handle this kind of nesting. The server complains with a SOQL syntax error.

            throw new IllegalArgumentException("Beans with parent-to-child fields cannot be referenced polymorphically");

        } else if (StringUtils.isEmpty(context.getPrefix())) {
            return new SoqlBuilder(accessor)
                .object(field.getRelatedObject())
                .template("(SELECT * from " + expandSimpleField(field, context) + ")")
                .depth(context.getRemainingDepth() - 1)
                .build();
        } else {
            // The server can't handle this kind of nesting. The server complains with "First SObject of a nested query
            // must be a child of its outer query".

            throw new IllegalArgumentException("Beans with parent-to-child fields cannot be referenced indirectly");
        }
    }

    private static final class Context {
        private final String prefix;
        private final int remainingDepth;
        private final boolean referencedPolymorphically;

        Context(String prefix, int remainingDepth, boolean referencedPolymorphically) {
            this.prefix = prefix;
            this.remainingDepth = remainingDepth;
            this.referencedPolymorphically = referencedPolymorphically;
        }

        private String getPrefix() {
            return prefix;
        }

        private boolean isReferencedPolymorphically() {
            return referencedPolymorphically;
        }

        private int getRemainingDepth() {
            return remainingDepth;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Context context = (Context) o;

            if (referencedPolymorphically != context.referencedPolymorphically) return false;
            if (remainingDepth != context.remainingDepth) return false;
            if (prefix != null ? !prefix.equals(context.prefix) : context.prefix != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = prefix != null ? prefix.hashCode() : 0;
            result = 31 * result + remainingDepth;
            result = 31 * result + (referencedPolymorphically ? 1 : 0);
            return result;
        }
    }

    private static final class CacheKey {
        private final ObjectDescriptor descriptor;
        private final Context context;

        CacheKey(ObjectDescriptor descriptor, Context context) {
            this.descriptor = descriptor;
            this.context = context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (!context.equals(cacheKey.context)) return false;
            if (!descriptor.equals(cacheKey.descriptor)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = descriptor.hashCode();
            result = 31 * result + context.hashCode();
            return result;
        }
    }
}
