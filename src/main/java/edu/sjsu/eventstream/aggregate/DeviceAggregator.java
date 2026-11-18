package edu.sjsu.eventstream.aggregate;

import edu.sjsu.eventstream.model.DeviceEvent;
import edu.sjsu.eventstream.model.Severity;
import edu.sjsu.eventstream.report.DeviceReport;
import edu.sjsu.eventstream.report.DeviceStats;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Counts device events per severity level, and per device with the
 * most frequent severity for that device. When two severity levels
 * tie on a device, the higher severity wins (CRITICAL highest, then
 * ERROR, then WARN, then INFO).
 */
public final class DeviceAggregator implements Aggregator<DeviceEvent> {

    private final Map<Severity, Integer> severityCounts = new EnumMap<>(Severity.class);
    private final Map<String, Map<Severity, Integer>> countsByDevice = new TreeMap<>();

    @Override
    public Class<DeviceEvent> recordType() {
        return DeviceEvent.class;
    }

    @Override
    public String reportName() {
        return "devices";
    }

    @Override
    public void accept(DeviceEvent record) {
        severityCounts.merge(record.severity(), 1, Integer::sum);
        countsByDevice.computeIfAbsent(record.device(), key -> new EnumMap<>(Severity.class))
                .merge(record.severity(), 1, Integer::sum);
    }

    @Override
    public Object buildReport() {
        if (countsByDevice.isEmpty()) {
            return Map.of();
        }
        Map<Severity, Integer> bySeverity = new LinkedHashMap<>();
        bySeverity.put(Severity.CRITICAL, severityCounts.getOrDefault(Severity.CRITICAL, 0));
        bySeverity.put(Severity.ERROR, severityCounts.getOrDefault(Severity.ERROR, 0));
        bySeverity.put(Severity.WARN, severityCounts.getOrDefault(Severity.WARN, 0));
        bySeverity.put(Severity.INFO, severityCounts.getOrDefault(Severity.INFO, 0));

        Map<String, DeviceStats> byDevice = new TreeMap<>();
        for (Map.Entry<String, Map<Severity, Integer>> entry : countsByDevice.entrySet()) {
            int total = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
            byDevice.put(entry.getKey(),
                    new DeviceStats(total, mostFrequentSeverity(entry.getValue())));
        }
        return new DeviceReport(bySeverity, byDevice);
    }

    /** Highest count wins; a tie is broken by the higher severity rank. */
    private Severity mostFrequentSeverity(Map<Severity, Integer> counts) {
        Severity best = null;
        for (Map.Entry<Severity, Integer> entry : counts.entrySet()) {
            if (best == null
                    || entry.getValue() > counts.get(best)
                    || (entry.getValue().equals(counts.get(best))
                            && entry.getKey().rank() > best.rank())) {
                best = entry.getKey();
            }
        }
        return best;
    }
}
