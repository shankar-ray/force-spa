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
    <T> String create(T record) throws ObjectNotFoundException, UnauthorizedException;

    /**
     * Deletes a record.
     *
     * @param id          the Salesforce id of the persistent record.
     * @param recordClass the annotated class of the record's bean
     */
    <T> void delete(String id, Class<T> recordClass) throws RecordNotFoundException, UnauthorizedException;

    /**
     * Deletes a record.
     *
     * @param record the bean of the record to delete. The bean must contain a populated "id" field. Other fields can be
     *               populated but are ignored.
     */
    <T> void delete(T record) throws RecordNotFoundException, UnauthorizedException;

    /**
     * Get a record by id. Also known as "read" (the R in CRUD).
     *
     * @param id          the Salesforce id of the record
     * @param recordClass the annotated class of the record's bean
     * @param <T>         the type of record
     * @return the record
     */
    <T> T get(String id, Class<T> recordClass) throws RecordNotFoundException, UnauthorizedException;

    /**
     * Updates selected fields in an existing record.
     *
     * @param id            the Salesforce id of the persistent record.
     * @param recordChanges the bean containing new values to be persisted. This bean can be sparsely populated. Only
     *                      the non-null fields from the bean are updated in the persistent record. A null value in a
     *                      bean field means no change is desired for that field in the persistent record.
     */
    <T> void patch(String id, T recordChanges) throws RecordNotFoundException, UnauthorizedException;

    /**
     * Updates all fields in an existing record.
     *
     * @param id     the Salesforce id of the persistent record.
     * @param record the bean containing new values to be persisted. All of the updatable fields in the persistent
     *               record will be updated with values from the bean. A null value in a bean field will cause the
     *               corresponding field in the persistent record to be set to null.
     */
    <T> void update(String id, T record) throws RecordNotFoundException, UnauthorizedException;

    /**
     * Updates all fields in an existing record.
     *
     * @param record the bean containing new values to be persisted. The bean must contain a populated "id" field. All
     *               of the updatable fields in the persistent record will be updated with values from the bean. A null
     *               value in a bean field will cause the corresponding field in the persistent record to be set to
     *               null.
     */
    <T> void update(T record) throws RecordNotFoundException, UnauthorizedException;

    /**
     * Creates an instance of {@link RecordQuery} for executing a SOQL query.
     *
     * @param soqlTemplate a template for the SOQL query. The template is processed before execution to arrive at the
     *                     final SOQL query. The processing involves replacing occurrences of wildcards (*) with actual
     *                     field names as described by the annotations in the record class.
     * @param recordClass  the annotated class of the result record's bean
     * @param <T>          the type of record
     * @return a {link RecordQuery} which can be executed
     */
    <T> RecordQuery<T> createQuery(String soqlTemplate, Class<T> recordClass);

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
    void execute(RecordOperation<?>... operations);

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

    /**
     * Returns the {@link RecordAccessorConfig}.
     *
     * @return the record accessor config
     */
    RecordAccessorConfig getConfig();

    /**
     * Returns a new create operation that can be combined into a batch. The operation, along with any others that it is
     * batched with, can be executed at a later time using {@link #execute}.
     *
     * @param record the bean holding values to be persisted in the new record.
     * @return the operation
     */
    <T> CreateRecordOperation<T> newCreateRecordOperation(T record);

    /**
     * Returns a new delete operation that can be combined into a batch. The operation, along with any others that it is
     * batched with, can be executed at a later time using {@link #execute}.
     *
     * @param id          the Salesforce id of the persistent record.
     * @param recordClass the annotated class of the record's bean
     * @return the operation
     */
    <T> DeleteRecordOperation<T> newDeleteRecordOperation(String id, Class<T> recordClass);

    /**
     * Returns a new get operation that can be combined into a batch. The operation, along with any others that it is
     * batched with, can be executed at a later time using {@link #execute}.
     *
     * @param id          the Salesforce id of the persistent record.
     * @param recordClass the annotated class of the result record's bean
     * @return the operation
     */
    <T> GetRecordOperation<T> newGetRecordOperation(String id, Class<T> recordClass);

    /**
     * Returns a new patch operation that can be combined into a batch. The operation, along with any others that it is
     * batched with, can be executed at a later time using {@link #execute}.
     *
     * @param id            the Salesforce id of the persistent record.
     * @param recordChanges the bean containing new values to be persisted. This bean can be sparsely populated. Only
     *                      the non-null fields from the bean are updated in the persistent record. A null value in a
     *                      bean field means no change is desired for that field in the persistent record.
     * @return the operation
     */
    <T> PatchRecordOperation<T> newPatchRecordOperation(String id, T recordChanges);

    /**
     * Returns a new update operation that can be combined into a batch. The operation, along with any others that it is
     * batched with, can be executed at a later time using {@link #execute}.
     *
     * @param id     the Salesforce id of the persistent record.
     * @param record the bean containing new values to be persisted. All of the updatable fields in the persistent
     *               record will be updated with values from the bean. A null value in a bean field will cause the
     *               corresponding field in the persistent record to be set to null.
     * @return the operation
     */
    <T> UpdateRecordOperation<T> newUpdateRecordOperation(String id, T record);

    /**
     * Returns a new query operation that can be combined into a batch. The operation, along with any others that it is
     * batched with, can be executed at a later time using {@link #execute}.
     *
     * @param soqlTemplate a template for the SOQL query. The template is processed before execution to arrive at the
     *                     final SOQL query. The processing involves replacing occurrences of wildcards (*) with actual
     *                     field names as described by the annotations in the record class.
     * @param recordClass  the annotated class of the requested object's bean
     * @return the operation
     */
    <T> QueryRecordsOperation<T, T> newQueryRecordsOperation(String soqlTemplate, Class<T> recordClass);

    /**
     * Returns a new query operation that can be combined into a batch. The operation, along with any others that it is
     * batched with, can be executed at a later time using {@link #execute}.
     *
     * @param soqlTemplate a template for the SOQL query. The template is processed before execution to arrive at the
     *                     final SOQL query. The processing involves replacing occurrences of wildcards (*) with actual
     *                     field names as described by the annotations in the record class.
     * @param recordClass  the annotated class of the requested object's bean
     * @param resultClass  the class of the desired return type that differs from the record class. A typical choice is
     *                     com.fasterxml.jackson.databind.JsonNode.
     * @return the operation
     */
    <T, R> QueryRecordsOperation<T, R> newQueryRecordsOperation(String soqlTemplate, Class<T> recordClass, Class<R> resultClass);
}
