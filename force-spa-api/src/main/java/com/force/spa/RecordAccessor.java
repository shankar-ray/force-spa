/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa;

import java.util.List;

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
    <T> String create(T record);

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
    <T> void update(String id, T record);

    /**
     * Updates all fields in an existing record.
     *
     * @param record the bean containing new values to be persisted. The bean must contain a populated "id" field. All
     *               of the updatable fields in the persistent record will be updated with values from the bean. A null
     *               value in a bean field will cause the corresponding field in the persistent record to be set to
     *               null.
     */
    <T> void update(T record);

    /**
     * Updates selected fields in an existing record.
     *
     * @param id            the Salesforce id of the persistent record.
     * @param recordChanges the bean containing new values to be persisted. This bean can be sparsely populated. Only
     *                      the non-null fields from the bean are updated in the persistent record. A null value in a
     *                      bean field means no change is desired for that field in the persistent record.
     */
    <T> void patch(String id, T recordChanges);

    /**
     * Deletes a record.
     *
     * @param id          the Salesforce id of the persistent record.
     * @param recordClass the annotated class of the record's bean
     */
    <T> void delete(String id, Class<T> recordClass);

    /**
     * Deletes a record.
     *
     * @param record the bean of the record to delete. The bean must contain a populated "id" field. Other fields can be
     *               populated but are ignored.
     */
    <T> void delete(T record);

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

    /**
     * Executes a list of record operations.
     * <p/>
     * Interaction with each operation result is patterned after {@link java.util.concurrent.FutureTask}. The completion
     * status is determined by asking for the operation result (using {@link com.force.spa.RecordOperation#get()}). If
     * the operation is successful the result value is returned otherwise {@link java.util.concurrent.ExecutionException}
     * is thrown.
     *
     * @param operations the operations
     */
    void execute(RecordOperation<?> ... operations);

    /**
     * Executes a list of record operations.
     * <p/>
     * Interaction with each operation result is patterned after {@link java.util.concurrent.FutureTask}. The completion
     * status is determined by asking for the operation result (using {@link com.force.spa.RecordOperation#get()}). If
     * the operation is successful the result value is returned otherwise {@link java.util.concurrent.ExecutionException}
     * is thrown.
     *
     * @param operations the operations
     */
    void execute(List<RecordOperation<?>> operations);

    <T> CreateRecordOperation<T> newCreateRecordOperation(T record);

    <T> DeleteRecordOperation<T> newDeleteRecordOperation(String id, Class<T> recordClass);

    <T> GetRecordOperation<T> newGetRecordOperation(String id, Class<T> recordClass);

    <T> PatchRecordOperation<T> newPatchRecordOperation(String id, T record);

    <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soql, Class<?> recordClass, Class<T> resultClass);

    <T> QueryRecordsOperation<T> newQueryRecordsOperation(String soql, Class<T> recordClass);

    <T> UpdateRecordOperation<T> newUpdateRecordOperation(String id, T record);
}
