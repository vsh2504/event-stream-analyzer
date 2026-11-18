package edu.sjsu.eventstream.model;

/** A sensor reading: one numeric measurement for a named sensor. */
public record SensorReading(String sensor, double reading) implements EventRecord {
}
