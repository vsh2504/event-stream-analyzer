package edu.sjsu.eventstream.model;

/** A device event: a state change or alert with a severity. */
public record DeviceEvent(String device, Severity severity) implements EventRecord {
}
