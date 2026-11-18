package edu.sjsu.eventstream.model;

/** A sensor reading: one numeric measurement for a named sensor. */
public record SensorReading(String sensor, java.math.BigDecimal decimalReading) implements EventRecord {
    public SensorReading(String sensor, double reading) {
        this(sensor, java.math.BigDecimal.valueOf(reading));
    }

    public double reading() {
        return decimalReading.doubleValue();
    }
}
