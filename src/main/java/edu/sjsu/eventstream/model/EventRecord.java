package edu.sjsu.eventstream.model;

/**
 * A classified event record. The interface is sealed so the set of
 * record types is fixed in code; each record type has its own
 * classifier in the classification chain and its own aggregator.
 */
public sealed interface EventRecord
        permits SensorReading, DeviceEvent, AccessRecord {
}
