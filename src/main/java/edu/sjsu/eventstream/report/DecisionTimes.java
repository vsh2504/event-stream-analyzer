package edu.sjsu.eventstream.report;

import com.google.gson.annotations.SerializedName;

/** Decision time statistics for one door, all in milliseconds. */
public record DecisionTimes(
        long min,
        @SerializedName("50_percentile") long percentile50,
        @SerializedName("90_percentile") long percentile90,
        @SerializedName("99_percentile") long percentile99,
        long max) {
}
