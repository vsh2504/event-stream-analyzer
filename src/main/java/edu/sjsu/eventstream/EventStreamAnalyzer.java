package edu.sjsu.eventstream;

import edu.sjsu.eventstream.aggregate.AggregationEngine;
import edu.sjsu.eventstream.aggregate.ReportFile;
import edu.sjsu.eventstream.classify.RecordClassifier;
import edu.sjsu.eventstream.parsing.LineParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The processing pipeline. Each line flows through three separate
 * responsibilities: the parser turns text into key and value pairs,
 * the classifier chain turns pairs into a typed record, and the
 * aggregation engine routes the record to its aggregator. Lines that
 * fail parsing or match no record type are skipped silently.
 */
public final class EventStreamAnalyzer {

    private final LineParser parser;
    private final RecordClassifier classifierChain;
    private final AggregationEngine engine;

    public EventStreamAnalyzer(LineParser parser,
                               RecordClassifier classifierChain,
                               AggregationEngine engine) {
        this.parser = parser;
        this.classifierChain = classifierChain;
        this.engine = engine;
    }

    /** Reads the input file and returns one report per record type. */
    public List<ReportFile> analyze(Path inputFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parser.parse(line)
                        .flatMap(classifierChain::classify)
                        .ifPresent(engine::accept);
            }
        }
        return engine.buildReports();
    }
}
