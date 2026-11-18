package edu.sjsu.eventstream.report;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/** Aggregated access results for one door. */
public record DoorStats(
        @SerializedName("decision_times") DecisionTimes decisionTimes,
        Map<String, Integer> results,
        @SerializedName("flagged_users") List<String> flaggedUsers) {
}
