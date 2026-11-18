package edu.sjsu.eventstream.classify;

import edu.sjsu.eventstream.AnalyzerFactory;
import edu.sjsu.eventstream.model.AccessRecord;
import edu.sjsu.eventstream.model.DeviceEvent;
import edu.sjsu.eventstream.model.EventRecord;
import edu.sjsu.eventstream.model.SensorReading;
import edu.sjsu.eventstream.model.Severity;
import edu.sjsu.eventstream.parsing.KeyValueLineParser;
import edu.sjsu.eventstream.parsing.ParsedLine;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassifierChainTest {

    private final KeyValueLineParser parser = new KeyValueLineParser();
    private final RecordClassifier chain = AnalyzerFactory.createClassifierChain();

    private Optional<EventRecord> classify(String line) {
        ParsedLine parsed = parser.parse(line).orElseThrow();
        return chain.classify(parsed);
    }

    @Test
    void sensorLineClassifiesAsSensorReading() {
        Optional<EventRecord> result = classify(
                "ts=now sensor=temperature_celsius device=thermostat-01 reading=21.5");
        SensorReading reading = assertInstanceOf(SensorReading.class, result.orElseThrow());
        assertEquals("temperature_celsius", reading.sensor());
        assertEquals(21.5, reading.reading());
    }

    @Test
    void sensorLineWithNonNumericReadingIsSkipped() {
        assertTrue(classify(
                "ts=now sensor=temperature_celsius device=thermostat-01 reading=abc").isEmpty());
    }

    @Test
    void deviceLineClassifiesAsDeviceEvent() {
        Optional<EventRecord> result = classify(
                "ts=now severity=ERROR device=door-lock-07 msg=\"Latch jammed\" code=E41");
        DeviceEvent event = assertInstanceOf(DeviceEvent.class, result.orElseThrow());
        assertEquals("door-lock-07", event.device());
        assertEquals(Severity.ERROR, event.severity());
    }

    @Test
    void invalidSeverityIsSkipped() {
        assertTrue(classify("ts=now severity=DEBUG device=thermostat-01").isEmpty());
    }

    @Test
    void severityWithoutDeviceIsSkipped() {
        assertTrue(classify("ts=now severity=INFO msg=\"Heartbeat OK\"").isEmpty());
    }

    @Test
    void accessLineClassifiesAsAccessRecord() {
        Optional<EventRecord> result = classify(
                "ts=now user=user-42 door=door-A result=DENIED duration_ms=30");
        AccessRecord record = assertInstanceOf(AccessRecord.class, result.orElseThrow());
        assertEquals("user-42", record.user());
        assertEquals("door-A", record.door());
        assertEquals("DENIED", record.result());
        assertEquals(30, record.durationMs());
    }

    @Test
    void accessLineWithInvalidResultIsSkipped() {
        assertTrue(classify(
                "ts=now user=user-42 door=door-A result=MAYBE duration_ms=30").isEmpty());
    }

    @Test
    void accessLineWithNonIntegerDurationIsSkipped() {
        assertTrue(classify(
                "ts=now user=user-42 door=door-A result=GRANTED duration_ms=12.5").isEmpty());
    }

    @Test
    void firstMatchWinsWhenLineMatchesTwoTypes() {
        Optional<EventRecord> result = classify(
                "ts=now sensor=temperature_celsius reading=20 severity=INFO device=thermostat-01");
        assertInstanceOf(SensorReading.class, result.orElseThrow());
    }

    @Test
    void lineWithOnlyTimestampIsSkipped() {
        assertTrue(classify("ts=2024-05-10T08:00:00Z").isEmpty());
    }

    @Test
    void negativeReadingIsAValidSensorReading() {
        Optional<EventRecord> result = classify(
                "ts=now sensor=temperature_celsius reading=-3.5");
        SensorReading reading = assertInstanceOf(SensorReading.class, result.orElseThrow());
        assertEquals(-3.5, reading.reading());
    }

    @Test
    void crlfTerminatedLineStillClassifies() {
        Optional<EventRecord> result = classify(
                "ts=now device=thermostat-01 severity=WARN\r");
        DeviceEvent event = assertInstanceOf(DeviceEvent.class, result.orElseThrow());
        assertEquals(Severity.WARN, event.severity());
    }

    @Test
    void lowercaseSeverityIsSkipped() {
        assertTrue(classify("ts=now severity=info device=thermostat-01").isEmpty());
    }
}
