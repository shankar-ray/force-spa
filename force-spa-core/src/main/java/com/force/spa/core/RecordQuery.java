/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core;

import java.util.List;

/**
 * An interface for controlling the execution of SOQL queries.
 *
 * @param <T> the type of result records
 */
public interface RecordQuery<T> {
    /**
     * Execute a SOQL query and return the list of records satisfying the query.
     *
     * @return the list of records satisfying the query
     */
    List<T> execute();

    /**
     * Execute a SOQL query and return the list of records satisfying the query.
     * <p/>
     * This method is used to request the results in an alternate Java form. For example, instead of getting the results
     * back in the form of the originally annotated bean, you can use this to request the results back as {@link
     * com.fasterxml.jackson.databind.JsonNode} instead. This is useful if you want to manually navigate the raw results
     * in order to access things like aggregate fields.
     *
     * @param resultClass the class of the desired return type. A typical choice is {@link
     *                    com.fasterxml.jackson.databind.JsonNode}.
     * @param <R>         the type of result records
     * @return the list of records satisfying the query
     */
    <R> List<R> execute(Class<R> resultClass);

    /**
     * Sets the maximum number of results to retrieve.
     *
     * @param maxResult the maximum number of results to retrieve
     * @return the same query instance
     */
    RecordQuery<T> setMaxResults(int maxResult);

    /**
     * Sets the position of the first result to retrieve.
     *
     * @param startPosition the position of the first result to retrieve
     * @return the same query instance
     */
    RecordQuery<T> setFirstResult(int startPosition);
}
