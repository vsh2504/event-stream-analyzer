package edu.sjsu.eventstream.report;

import com.google.gson.annotations.SerializedName;
import edu.sjsu.eventstream.model.Severity;

/** Event summary for one device. */
public record DeviceStats(
        int total,
        @SerializedName("most_frequent_severity") Severity mostFrequentSeverity) {
}
