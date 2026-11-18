package edu.sjsu.eventstream.aggregate;

/**
 * One finished report: the output file name and the format neutral
 * report model. Writers (JSON today, CSV in the future) serialize the
 * model without knowing how it was computed.
 */
public record ReportFile(String reportName, Object model) {
}
