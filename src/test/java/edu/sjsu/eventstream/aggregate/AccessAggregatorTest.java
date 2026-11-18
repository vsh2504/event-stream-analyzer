package edu.sjsu.eventstream.aggregate;

import edu.sjsu.eventstream.model.AccessRecord;
import edu.sjsu.eventstream.report.DoorStats;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessAggregatorTest {

    @SuppressWarnings("unchecked")
    private Map<String, DoorStats> reportOf(AccessAggregator aggregator) {
        return (Map<String, DoorStats>) aggregator.buildReport();
    }

    private void accept(AccessAggregator aggregator, String user, String door,
                        String result, long durationMs) {
        aggregator.accept(new AccessRecord(user, door, result, durationMs));
    }

    @Test
    void percentilesUseNearestRankMethod() {
        AccessAggregator aggregator = new AccessAggregator();
        accept(aggregator, "user-42", "door-A", "DENIED", 30);
        accept(aggregator, "user-42", "door-A", "DENIED", 28);
        accept(aggregator, "user-42", "door-A", "DENIED", 35);
        accept(aggregator, "user-08", "door-A", "GRANTED", 18);

        DoorStats stats = reportOf(aggregator).get("door-A");
        // sorted durations are 18, 28, 30, 35
        assertEquals(18, stats.decisionTimes().min());
        assertEquals(28, stats.decisionTimes().percentile50());
        assertEquals(35, stats.decisionTimes().percentile90());
        assertEquals(35, stats.decisionTimes().percentile99());
        assertEquals(35, stats.decisionTimes().max());
    }

    @Test
    void fiftiethPercentileIsNotTheMedian() {
        AccessAggregator aggregator = new AccessAggregator();
        accept(aggregator, "user-1", "door-A", "GRANTED", 10);
        accept(aggregator, "user-1", "door-A", "GRANTED", 20);
        accept(aggregator, "user-1", "door-A", "GRANTED", 30);
        accept(aggregator, "user-1", "door-A", "GRANTED", 40);

        // nearest-rank gives the value at index ceil(0.5 * 4) - 1 = 1, which is 20,
        // while the median of these values would be 25
        assertEquals(20, reportOf(aggregator).get("door-A").decisionTimes().percentile50());
    }

    @Test
    void resultsCountGrantedAndDeniedIncludingZeroes() {
        AccessAggregator aggregator = new AccessAggregator();
        accept(aggregator, "user-17", "door-B", "GRANTED", 12);
        accept(aggregator, "user-08", "door-B", "GRANTED", 15);

        DoorStats stats = reportOf(aggregator).get("door-B");
        assertEquals(2, stats.results().get("GRANTED"));
        assertEquals(0, stats.results().get("DENIED"));
    }

    @Test
    void usersWithThreeOrMoreDeniedAttemptsOnADoorAreFlagged() {
        AccessAggregator aggregator = new AccessAggregator();
        accept(aggregator, "user-42", "door-A", "DENIED", 30);
        accept(aggregator, "user-42", "door-A", "DENIED", 28);
        accept(aggregator, "user-42", "door-A", "DENIED", 35);
        accept(aggregator, "user-99", "door-A", "DENIED", 22);
        accept(aggregator, "user-99", "door-A", "DENIED", 24);
        accept(aggregator, "user-99", "door-B", "DENIED", 20);

        Map<String, DoorStats> report = reportOf(aggregator);
        // user-42 has exactly 3 denied attempts on door-A and is flagged;
        // user-99 has only 2 on door-A, and the third denial is on another door
        assertEquals(java.util.List.of("user-42"), report.get("door-A").flaggedUsers());
        assertTrue(report.get("door-B").flaggedUsers().isEmpty());
    }

    @Test
    void twoRecordPercentilesUseNearestRank() {
        AccessAggregator aggregator = new AccessAggregator();
        accept(aggregator, "user-1", "door-A", "GRANTED", 10);
        accept(aggregator, "user-1", "door-A", "GRANTED", 30);

        DoorStats stats = reportOf(aggregator).get("door-A");
        // rank for p50 is ceil(0.5 * 2) = 1, so the smaller value wins;
        // rank for p90 is ceil(0.9 * 2) = 2, so the larger value wins
        assertEquals(10, stats.decisionTimes().percentile50());
        assertEquals(30, stats.decisionTimes().percentile90());
        assertEquals(30, stats.decisionTimes().percentile99());
    }

    @Test
    void singleRecordDoorHasAllPercentilesEqual() {
        AccessAggregator aggregator = new AccessAggregator();
        accept(aggregator, "user-17", "door-B", "GRANTED", 12);

        DoorStats stats = reportOf(aggregator).get("door-B");
        assertEquals(12, stats.decisionTimes().min());
        assertEquals(12, stats.decisionTimes().percentile50());
        assertEquals(12, stats.decisionTimes().percentile90());
        assertEquals(12, stats.decisionTimes().percentile99());
        assertEquals(12, stats.decisionTimes().max());
    }
}
