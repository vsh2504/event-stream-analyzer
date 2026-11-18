package edu.sjsu.eventstream.report;

import com.google.gson.annotations.SerializedName;

/**
 * Aggregated statistics for one sensor. Decimal fields hold whole
 * numbers when the rounded result is an integer (see Statistics),
 * so they are typed as Number.
 */
public record SensorStats(
        int count,
        Number minimum,
        Number median,
        Number average,
        Number maximum,
        @SerializedName("std_dev") Number stdDev) {
}
