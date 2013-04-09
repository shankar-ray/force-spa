/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

/**
 * A CRUD-based interface for interacting with persistent records in Salesforce through the use of annotated Javabeans.
 */
public interface RecordAccessor {
    /**
     * Creates a new record.
     *
     * @param record the bean holding values to be persisted in the new record.
     * @return the id of the persisted record
     */
    String create(Object record);

    /**
     * Get a record by id. Also known as "read" (the R in CRUD).
     *
     * @param id          the Salesforce id of the record
     * @param recordClass the annotated class of the record's bean
     * @param <T>         the type of record
     * @return the record
     */
    <T> T get(String id, Class<T> recordClass);

    /**
     * Updates all fields in an existing record.
     *
     * @param id     the Salesforce id of the persistent record.
     * @param record the bean containing new values to be persisted. All of the updatable fields in the persistent
     *               record will be updated with values from the bean. A null value in a bean field will cause the
     *               corresponding field in the persistent record to be set to null.
     */
    void update(String id, Object record);

    /**
     * Updates all fields in an existing record.
     *
     * @param record the bean containing new values to be persisted. The bean must contain a populated "id" field. All
     *               of the updatable fields in the persistent record will be updated with values from the bean. A null
     *               value in a bean field will cause the corresponding field in the persistent record to be set to
     *               null.
     */
    void update(Object record);

    /**
     * Updates selected fields in an existing record.
     *
     * @param id            the Salesforce id of the persistent record.
     * @param recordChanges the bean containing new values to be persisted. This bean can be sparsely populated. Only
     *                      the non-null fields from the bean are updated in the persistent record. A null value in a
     *                      bean field means no change is desired for that field in the persistent record.
     */
    void patch(String id, Object recordChanges);

    /**
     * Deletes a record.
     *
     * @param id          the Salesforce id of the persistent record.
     * @param recordClass the annotated class of the record's bean
     */
    void delete(String id, Class<?> recordClass);

    /**
     * Deletes a record.
     *
     * @param record the bean of the record to delete. The bean must contain a populated "id" field. Other fields can be
     *               populated but are ignored.
     */
    void delete(Object record);

    /**
     * Creates an instance of {@link RecordQuery} for executing a SOQL query.
     *
     * @param qualification a template for the SOQL query. The template is processed before execution to arrive at the
     *                      final SOQL query. The processing involves replacing occurrences of wildcards (*) with actual
     *                      field names as described by the annotations in the record class.
     * @param recordClass   the annotated class of the result record's bean
     * @param <T>           the type of result records
     * @return a {link RecordQuery} which can be executed
     */
    <T> RecordQuery<T> createQuery(String qualification, Class<T> recordClass);
}
