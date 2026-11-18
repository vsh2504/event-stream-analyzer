package edu.sjsu.eventstream.aggregate;

import edu.sjsu.eventstream.model.AccessRecord;
import edu.sjsu.eventstream.report.DecisionTimes;
import edu.sjsu.eventstream.report.DoorStats;
import edu.sjsu.eventstream.stats.Statistics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Aggregates access records per door: decision time statistics
 * (min, max, and the 50th, 90th, and 99th percentiles by the
 * nearest-rank method), GRANTED and DENIED counts, and the users
 * with 3 or more denied attempts on the door.
 */
public final class AccessAggregator implements Aggregator<AccessRecord> {

    static final int DENIED_FLAG_THRESHOLD = 3;

    private final Map<String, List<Long>> durationsByDoor = new TreeMap<>();
    private final Map<String, Map<String, Integer>> resultsByDoor = new TreeMap<>();
    private final Map<String, Map<String, Integer>> deniedByDoorAndUser = new TreeMap<>();

    @Override
    public Class<AccessRecord> recordType() {
        return AccessRecord.class;
    }

    @Override
    public String reportName() {
        return "access";
    }

    @Override
    public void accept(AccessRecord record) {
        durationsByDoor.computeIfAbsent(record.door(), key -> new ArrayList<>())
                .add(record.durationMs());
        resultsByDoor.computeIfAbsent(record.door(), key -> new LinkedHashMap<>())
                .merge(record.result(), 1, Integer::sum);
        if (record.isDenied()) {
            deniedByDoorAndUser.computeIfAbsent(record.door(), key -> new TreeMap<>())
                    .merge(record.user(), 1, Integer::sum);
        }
    }

    @Override
    public Object buildReport() {
        Map<String, DoorStats> report = new TreeMap<>();
        for (Map.Entry<String, List<Long>> entry : durationsByDoor.entrySet()) {
            String door = entry.getKey();
            List<Long> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(Long::compareTo);
            DecisionTimes times = new DecisionTimes(
                    sorted.get(0),
                    Statistics.percentileNearestRank(sorted, 50),
                    Statistics.percentileNearestRank(sorted, 90),
                    Statistics.percentileNearestRank(sorted, 99),
                    sorted.get(sorted.size() - 1));
            Map<String, Integer> results = new LinkedHashMap<>();
            results.put("GRANTED", resultsByDoor.get(door).getOrDefault("GRANTED", 0));
            results.put("DENIED", resultsByDoor.get(door).getOrDefault("DENIED", 0));
            List<String> flaggedUsers = new ArrayList<>();
            for (Map.Entry<String, Integer> userCount
                    : deniedByDoorAndUser.getOrDefault(door, Map.of()).entrySet()) {
                if (userCount.getValue() >= DENIED_FLAG_THRESHOLD) {
                    flaggedUsers.add(userCount.getKey());
                }
            }
            flaggedUsers.sort(String::compareTo);
            report.put(door, new DoorStats(times, results, flaggedUsers));
        }
        return report;
    }
}
