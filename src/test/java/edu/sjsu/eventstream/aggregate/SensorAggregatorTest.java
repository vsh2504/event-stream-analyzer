package edu.sjsu.eventstream.aggregate;

import edu.sjsu.eventstream.model.SensorReading;
import edu.sjsu.eventstream.report.SensorStats;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorAggregatorTest {

    @SuppressWarnings("unchecked")
    private Map<String, SensorStats> reportOf(SensorAggregator aggregator) {
        return (Map<String, SensorStats>) aggregator.buildReport();
    }

    @Test
    void evenCountStatsUseAverageOfTwoMiddleValues() {
        SensorAggregator aggregator = new SensorAggregator();
        aggregator.accept(new SensorReading("temperature_celsius", 18.5));
        aggregator.accept(new SensorReading("temperature_celsius", 20.0));
        aggregator.accept(new SensorReading("temperature_celsius", 22.0));
        aggregator.accept(new SensorReading("temperature_celsius", 25.0));

        SensorStats stats = reportOf(aggregator).get("temperature_celsius");
        assertEquals(4, stats.count());
        assertEquals(18.5, stats.minimum().doubleValue());
        assertEquals(21.0, stats.median().doubleValue());
        assertEquals(21.4, stats.average().doubleValue());
        assertEquals(25.0, stats.maximum().doubleValue());
        // population standard deviation of the four values is 2.43..., rounded to 2.4
        assertEquals(2.4, stats.stdDev().doubleValue());
    }

    @Test
    void oddCountStatsUseMiddleValue() {
        SensorAggregator aggregator = new SensorAggregator();
        aggregator.accept(new SensorReading("pressure_hpa", 10));
        aggregator.accept(new SensorReading("pressure_hpa", 30));
        aggregator.accept(new SensorReading("pressure_hpa", 20));

        SensorStats stats = reportOf(aggregator).get("pressure_hpa");
        assertEquals(3, stats.count());
        assertEquals(20.0, stats.median().doubleValue());
        assertEquals(20.0, stats.average().doubleValue());
        assertEquals(10.0, stats.minimum().doubleValue());
        assertEquals(30.0, stats.maximum().doubleValue());
        assertEquals(8.2, stats.stdDev().doubleValue());
    }

    @Test
    void resultsAreRoundedToOneDecimalWithHalfUp() {
        SensorAggregator aggregator = new SensorAggregator();
        aggregator.accept(new SensorReading("temperature_celsius", 21.5));
        aggregator.accept(new SensorReading("temperature_celsius", 25.0));

        SensorStats stats = reportOf(aggregator).get("temperature_celsius");
        // median and average are exactly 23.25, which rounds half up to 23.3
        assertEquals(23.3, stats.median().doubleValue());
        assertEquals(23.3, stats.average().doubleValue());
        // population standard deviation is exactly 1.75, which rounds half up to 1.8
        assertEquals(1.8, stats.stdDev().doubleValue());
    }

    @Test
    void wholeNumberResultsHaveNoDecimalPoint() {
        SensorAggregator aggregator = new SensorAggregator();
        aggregator.accept(new SensorReading("humidity_percent", 45));
        aggregator.accept(new SensorReading("humidity_percent", 50));

        SensorStats stats = reportOf(aggregator).get("humidity_percent");
        assertInstanceOf(Long.class, stats.minimum());
        assertInstanceOf(Long.class, stats.maximum());
        assertEquals(45L, stats.minimum().longValue());
        assertEquals(50L, stats.maximum().longValue());
    }

    @Test
    void readingsAreGroupedBySensorName() {
        SensorAggregator aggregator = new SensorAggregator();
        aggregator.accept(new SensorReading("temperature_celsius", 20));
        aggregator.accept(new SensorReading("humidity_percent", 40));
        aggregator.accept(new SensorReading("temperature_celsius", 22));
        aggregator.accept(new SensorReading("co2_ppm", 900));

        Map<String, SensorStats> report = reportOf(aggregator);
        assertEquals(3, report.size());
        assertEquals(2, report.get("temperature_celsius").count());
        assertEquals(1, report.get("humidity_percent").count());
        assertEquals(1, report.get("co2_ppm").count());
    }

    @Test
    void emptyAggregatorProducesEmptyReport() {
        assertTrue(reportOf(new SensorAggregator()).isEmpty());
    }
}
