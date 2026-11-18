package edu.sjsu.eventstream;

import edu.sjsu.eventstream.aggregate.AccessAggregator;
import edu.sjsu.eventstream.aggregate.AggregationEngine;
import edu.sjsu.eventstream.aggregate.DeviceAggregator;
import edu.sjsu.eventstream.aggregate.SensorAggregator;
import edu.sjsu.eventstream.classify.AccessRecordClassifier;
import edu.sjsu.eventstream.classify.DeviceEventClassifier;
import edu.sjsu.eventstream.classify.RecordClassifier;
import edu.sjsu.eventstream.classify.SensorReadingClassifier;
import edu.sjsu.eventstream.output.JsonReportWriter;
import edu.sjsu.eventstream.output.ReportWriter;
import edu.sjsu.eventstream.parsing.KeyValueLineParser;
import edu.sjsu.eventstream.parsing.LineParser;

/**
 * Builds and wires the application components (Factory pattern).
 * All construction knowledge lives here: the order of the classifier
 * chain, which aggregators exist, and which writer a format name
 * maps to. Supporting a new record type or format means registering
 * it here, in one place.
 */
public final class AnalyzerFactory {

    private AnalyzerFactory() {
    }

    public static LineParser createLineParser() {
        return new KeyValueLineParser();
    }

    /**
     * The classifier chain, in precedence order: sensor readings
     * first, then device events, then access records.
     */
    public static RecordClassifier createClassifierChain() {
        RecordClassifier head = new SensorReadingClassifier();
        head.setNext(new DeviceEventClassifier())
                .setNext(new AccessRecordClassifier());
        return head;
    }

    public static AggregationEngine createAggregationEngine() {
        AggregationEngine engine = new AggregationEngine();
        engine.register(new SensorAggregator());
        engine.register(new DeviceAggregator());
        engine.register(new AccessAggregator());
        return engine;
    }

    /** Creates the writer for a format name ("json" today). */
    public static ReportWriter createReportWriter(String format) {
        if ("json".equalsIgnoreCase(format)) {
            return new JsonReportWriter();
        }
        throw new IllegalArgumentException("Unsupported output format: " + format);
    }

    /** A fully wired analyzer: parser, chain, and a fresh engine. */
    public static EventStreamAnalyzer createAnalyzer() {
        return new EventStreamAnalyzer(
                createLineParser(),
                createClassifierChain(),
                createAggregationEngine());
    }
}
