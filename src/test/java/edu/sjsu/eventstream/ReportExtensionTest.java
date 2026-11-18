package edu.sjsu.eventstream;

import edu.sjsu.eventstream.aggregate.ReportFile;
import edu.sjsu.eventstream.output.JsonReportWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportExtensionTest {

    @TempDir
    Path directory;

    @Test
    void writerOwnsExtensionAndSupportsAnUnknownReportType() throws Exception {
        new JsonReportWriter().write(directory,
                List.of(new ReportFile("network", Map.of("bytes_in", 42))));
        assertTrue(Files.readString(directory.resolve("network.json")).contains("42"));
    }

    @Test
    void unknownEmptyReportDoesNotRequireWriterTypeChecks() throws Exception {
        new JsonReportWriter().write(directory,
                List.of(new ReportFile("network", Map.of())));
        assertEquals("{}", Files.readString(directory.resolve("network.json")).trim());
    }
}

