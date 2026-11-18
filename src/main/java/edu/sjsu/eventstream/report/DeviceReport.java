package edu.sjsu.eventstream.report;

import com.google.gson.annotations.SerializedName;
import edu.sjsu.eventstream.model.Severity;

import java.util.Map;

/** The full devices report: counts by severity and by device. */
public record DeviceReport(
        @SerializedName("by_severity") Map<Severity, Integer> bySeverity,
        @SerializedName("by_device") Map<String, DeviceStats> byDevice) {

    public boolean isEmpty() {
        return byDevice.isEmpty();
    }
}
