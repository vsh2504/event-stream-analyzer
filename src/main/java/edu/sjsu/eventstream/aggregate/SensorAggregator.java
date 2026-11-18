package edu.sjsu.eventstream.aggregate;

import edu.sjsu.eventstream.model.SensorReading;
import edu.sjsu.eventstream.report.SensorStats;
import edu.sjsu.eventstream.stats.Statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Groups readings by sensor name and computes count, minimum, median,
 * average, maximum, and population standard deviation per sensor.
 * Sensor names are not a fixed set: whatever name appears in the
 * input becomes a group.
 */
public final class SensorAggregator implements Aggregator<SensorReading> {

    private final Map<String, List<Double>> valuesBySensor = new TreeMap<>();

    @Override
    public Class<SensorReading> recordType() {
        return SensorReading.class;
    }

    @Override
    public String fileName() {
        return "sensors.json";
    }

    @Override
    public void accept(SensorReading record) {
        valuesBySensor.computeIfAbsent(record.sensor(), key -> new ArrayList<>())
                .add(record.reading());
    }

    @Override
    public Object buildReport() {
        Map<String, SensorStats> report = new TreeMap<>();
        for (Map.Entry<String, List<Double>> entry : valuesBySensor.entrySet()) {
            List<Double> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(Double::compareTo);
            double mean = Statistics.mean(sorted);
            SensorStats stats = new SensorStats(
                    sorted.size(),
                    Statistics.roundToOneDecimal(sorted.get(0)),
                    Statistics.roundToOneDecimal(Statistics.medianOfSorted(sorted)),
                    Statistics.roundToOneDecimal(mean),
                    Statistics.roundToOneDecimal(sorted.get(sorted.size() - 1)),
                    Statistics.roundToOneDecimal(
                            Statistics.populationStdDev(sorted, mean)));
            report.put(entry.getKey(), stats);
        }
        return report;
    }
}
