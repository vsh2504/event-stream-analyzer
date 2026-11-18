package edu.sjsu.eventstream.aggregate;

import edu.sjsu.eventstream.model.EventRecord;

/**
 * Aggregation strategy for one record type (Strategy pattern). Each
 * aggregator collects the records of its type while the input streams
 * by, then builds a format neutral report model. A new record type
 * gets a new implementation; existing aggregators do not change.
 */
public interface Aggregator<T extends EventRecord> {

    /** The record type this aggregator consumes. */
    Class<T> recordType();

    /** The output file this aggregator's report belongs in. */
    String fileName();

    void accept(T record);

    /** Builds the report model, empty when no records were accepted. */
    Object buildReport();
}
