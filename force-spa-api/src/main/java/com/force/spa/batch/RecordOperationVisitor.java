/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.batch;

public interface RecordOperationVisitor {

    void visit(CreateRecordOperation operation);

    void visit(DeleteRecordOperation operation);

    void visit(GetRecordOperation<?> operation);

    void visit(PatchRecordOperation operation);

    void visit(UpdateRecordOperation operation);

    void visit(QueryRecordsOperation<?> operation);
}
