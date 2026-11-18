package edu.sjsu.eventstream.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.sjsu.eventstream.aggregate.ReportFile;
import edu.sjsu.eventstream.report.DeviceReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Writes one pretty printed JSON file per report, with 2-space
 * indentation. A report with no records is still written, as an
 * empty JSON object ({}), so all three output files always exist.
 */
public final class JsonReportWriter implements ReportWriter {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    @Override
    public void write(Path outdir, List<ReportFile> reports) throws IOException {
        Files.createDirectories(outdir);
        for (ReportFile report : reports) {
            String json = isEmptyReport(report.model())
                    ? "{}"
                    : gson.toJson(report.model());
            Files.writeString(outdir.resolve(report.fileName()), json + "\n");
        }
    }

    private boolean isEmptyReport(Object model) {
        if (model instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        if (model instanceof DeviceReport deviceReport) {
            return deviceReport.isEmpty();
        }
        return false;
    }
}
