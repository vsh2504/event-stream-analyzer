package edu.sjsu.eventstream.model;

/** An access record: one badge or credential attempt at a door. */
public record AccessRecord(String user, String door, String result, long durationMs)
        implements EventRecord {

    public boolean isDenied() {
        return "DENIED".equals(result);
    }
}
