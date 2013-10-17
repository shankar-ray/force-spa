/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.rest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.force.spa.QueryRecordsExOperation;
import com.force.spa.QueryRecordsOperation;
import com.force.spa.Statistics;
import com.force.spa.TooManyQueryRowsException;
import com.force.spa.core.SoqlBuilder;
import com.force.spa.core.utils.CountingJsonParser;
import com.google.common.net.UrlEscapers;

final class RestQueryRecordsOperation<T, R> extends AbstractRestRecordOperation<T, List<R>> implements QueryRecordsOperation<R>, QueryRecordsExOperation<T, R> {

    private static final int INITIAL_ARRAY_ALLOCATION_SIZE = 500;  // Avoid a few growth cycles, but don't waste too much space

    private final String soqlTemplate;
    private final Class<T> recordClass;
    private final Class<R> resultClass;
    private int startPosition;
    private int maxResults;

    private String soql;

    RestQueryRecordsOperation(RestRecordAccessor accessor, String soqlTemplate, Class<T> recordClass, Class<R> resultClass) {
        super(accessor, recordClass);

        this.soqlTemplate = soqlTemplate;
        this.resultClass = resultClass;
        this.recordClass = recordClass;
        this.startPosition = 0;
        this.maxResults = 0;
    }

    @Override
    public String getSoqlTemplate() {
        return soqlTemplate;
    }

    @Override
    public Class<T> getRecordClass() {
        return recordClass;
    }

    @Override
    public Class<R> getResultClass() {
        return resultClass;
    }

    @Override
    public int getStartPosition() {
        return startPosition;
    }

    @Override
    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    protected void start(RestConnector connector) {

        soql = new SoqlBuilder(getRecordAccessor())
            .object(getObjectDescriptor())
            .template(soqlTemplate)
            .offset(startPosition)
            .limit(maxResults)
            .build();

        Statistics.Builder statisticsBuilder = new Statistics.Builder();
        URI queryUri = URI.create("/query?q=" + UrlEscapers.urlFormParameterEscaper().escape(soql));
        accumulateRecords(queryUri, connector, new ArrayList<R>(INITIAL_ARRAY_ALLOCATION_SIZE), statisticsBuilder);
    }

    private void accumulateRecords(URI uri, final RestConnector connector, final List<R> records, final Statistics.Builder statisticsBuilder) {

        connector.get(uri, new RestResponseHandler<QueryResult>() {
            @Override
            public QueryResult deserialize(CountingJsonParser parser) throws IOException {
                return QueryResult.deserialize(parser, resultClass);
            }

            @Override
            public void completed(QueryResult queryResult, Statistics statistics) {

                addStatisticsTo(statisticsBuilder, statistics);
                addQueryResultTo(statisticsBuilder, queryResult);

                for (Object record : queryResult.getRecords()) {
                    records.add(resultClass.cast(record));
                }

                if (queryResult.getNextRecordsUrl() != null) {
                    if (connector.isSynchronous()) {
                        accumulateRecords(URI.create(queryResult.getNextRecordsUrl()), connector, records, statisticsBuilder);
                    } else {
                        throw new TooManyQueryRowsException(); // Can only get one set of results when asynchronous
                    }
                } else {
                    RestQueryRecordsOperation.this.completed(records, statisticsBuilder.build());
                }
            }

            @Override
            public void failed(Throwable exception, Statistics statistics) {
                addStatisticsTo(statisticsBuilder, statistics);

                RestQueryRecordsOperation.this.failed(exception, statisticsBuilder.build());
            }
        });
    }

    private static void addQueryResultTo(Statistics.Builder accumulatedStatistics, QueryResult queryResult) {
        accumulatedStatistics.additionalRowsProcessed(queryResult.getRecords().size());
        accumulatedStatistics.totalRows(queryResult.getTotalSize());
    }

    private static void addStatisticsTo(Statistics.Builder accumulatedStatistics, Statistics statistics) {
        accumulatedStatistics.additionalBytesReceived(statistics.getBytesReceived());
        accumulatedStatistics.additionalBytesSent(statistics.getBytesSent());
        accumulatedStatistics.additionalElapsedNanos(statistics.getElapsedNanos());
    }

    @Override
    public String toString() {
        String string = "Query " + getObjectDescriptor().getName();
        if (getLogger().isDebugEnabled()) {
            string += ": " + soql;
        }
        return string;
    }
}
