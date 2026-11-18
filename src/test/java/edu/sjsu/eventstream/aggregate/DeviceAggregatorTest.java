package edu.sjsu.eventstream.aggregate;

import edu.sjsu.eventstream.model.DeviceEvent;
import edu.sjsu.eventstream.model.Severity;
import edu.sjsu.eventstream.report.DeviceReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceAggregatorTest {

    private DeviceReport reportOf(DeviceAggregator aggregator) {
        return (DeviceReport) aggregator.buildReport();
    }

    @Test
    void countsEventsPerSeverityIncludingZeroes() {
        DeviceAggregator aggregator = new DeviceAggregator();
        aggregator.accept(new DeviceEvent("thermostat-01", Severity.INFO));
        aggregator.accept(new DeviceEvent("thermostat-01", Severity.INFO));
        aggregator.accept(new DeviceEvent("door-lock-07", Severity.ERROR));

        DeviceReport report = reportOf(aggregator);
        assertEquals(0, report.bySeverity().get(Severity.CRITICAL));
        assertEquals(1, report.bySeverity().get(Severity.ERROR));
        assertEquals(0, report.bySeverity().get(Severity.WARN));
        assertEquals(2, report.bySeverity().get(Severity.INFO));
    }

    @Test
    void countsEventsPerDeviceWithMostFrequentSeverity() {
        DeviceAggregator aggregator = new DeviceAggregator();
        aggregator.accept(new DeviceEvent("thermostat-01", Severity.INFO));
        aggregator.accept(new DeviceEvent("thermostat-01", Severity.INFO));
        aggregator.accept(new DeviceEvent("thermostat-01", Severity.WARN));
        aggregator.accept(new DeviceEvent("thermostat-01", Severity.INFO));
        aggregator.accept(new DeviceEvent("door-lock-07", Severity.ERROR));

        DeviceReport report = reportOf(aggregator);
        assertEquals(4, report.byDevice().get("thermostat-01").total());
        assertEquals(Severity.INFO,
                report.byDevice().get("thermostat-01").mostFrequentSeverity());
        assertEquals(1, report.byDevice().get("door-lock-07").total());
        assertEquals(Severity.ERROR,
                report.byDevice().get("door-lock-07").mostFrequentSeverity());
    }

    @Test
    void tieForMostFrequentSeverityBreaksToHighestSeverity() {
        DeviceAggregator aggregator = new DeviceAggregator();
        aggregator.accept(new DeviceEvent("thermostat-01", Severity.INFO));
        aggregator.accept(new DeviceEvent("thermostat-01", Severity.WARN));
        aggregator.accept(new DeviceEvent("door-lock-07", Severity.ERROR));
        aggregator.accept(new DeviceEvent("door-lock-07", Severity.ERROR));
        aggregator.accept(new DeviceEvent("door-lock-07", Severity.CRITICAL));
        aggregator.accept(new DeviceEvent("door-lock-07", Severity.CRITICAL));

        DeviceReport report = reportOf(aggregator);
        // 1 INFO against 1 WARN: WARN is the higher severity
        assertEquals(Severity.WARN,
                report.byDevice().get("thermostat-01").mostFrequentSeverity());
        // 2 ERROR against 2 CRITICAL: CRITICAL is the higher severity
        assertEquals(Severity.CRITICAL,
                report.byDevice().get("door-lock-07").mostFrequentSeverity());
    }

    @Test
    void emptyAggregatorProducesEmptyReport() {
        assertTrue(reportOf(new DeviceAggregator()).isEmpty());
    }
}
