package edu.sjsu.eventstream.output;

import edu.sjsu.eventstream.aggregate.ReportFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes finished reports to an output directory (Strategy pattern:
 * the serialization format is interchangeable). Implementations
 * receive format neutral report models and own every formatting
 * decision of their format.
 */
public interface ReportWriter {

    void write(Path outdir, List<ReportFile> reports) throws IOException;
}
