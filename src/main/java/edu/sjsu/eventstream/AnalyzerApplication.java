package edu.sjsu.eventstream;

import edu.sjsu.eventstream.aggregate.ReportFile;
import edu.sjsu.eventstream.output.ReportWriter;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Command line handling. Usage:
 *
 *   java -jar analyzer.jar --file events.txt --outdir results/
 *
 * --file is required and --outdir defaults to the current directory.
 * A missing --file, a malformed argument list, or an input file that
 * does not exist prints a clear error and returns a non-zero code.
 */
public final class AnalyzerApplication {

    static final String USAGE =
            "Usage: java -jar analyzer.jar --file <filename.txt> [--outdir <directory>]";

    /** Runs the application and returns the process exit code. */
    public int run(String[] args, PrintStream out, PrintStream err) {
        String file = null;
        String outdir = ".";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--file" -> {
                    if (i + 1 >= args.length) {
                        return usageError(err, "Missing value for --file");
                    }
                    file = args[++i];
                }
                case "--outdir" -> {
                    if (i + 1 >= args.length) {
                        return usageError(err, "Missing value for --outdir");
                    }
                    outdir = args[++i];
                }
                default -> {
                    return usageError(err, "Unknown argument: " + args[i]);
                }
            }
        }
        if (file == null) {
            return usageError(err, "Missing required argument: --file");
        }
        Path input = Path.of(file);
        if (!Files.isRegularFile(input)) {
            err.println("Error: input file not found: " + file);
            return 1;
        }
        try {
            EventStreamAnalyzer analyzer = AnalyzerFactory.createAnalyzer();
            ReportWriter writer = AnalyzerFactory.createReportWriter("json");
            List<ReportFile> reports = analyzer.analyze(input);
            writer.write(Path.of(outdir), reports);
        } catch (IOException e) {
            err.println("Error: failed to process " + file + ": " + e.getMessage());
            return 1;
        }
        return 0;
    }

    private int usageError(PrintStream err, String message) {
        err.println("Error: " + message);
        err.println(USAGE);
        return 2;
    }
}
