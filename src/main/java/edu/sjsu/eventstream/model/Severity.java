package edu.sjsu.eventstream.model;

/**
 * Severity of a device event. The rank defines the severity order used
 * when breaking ties for the most frequent severity of a device:
 * CRITICAL is the highest, then ERROR, then WARN, then INFO.
 */
public enum Severity {
    INFO(0),
    WARN(1),
    ERROR(2),
    CRITICAL(3);

    private final int rank;

    Severity(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    /** Returns the severity for an exact value such as "WARN", or null. */
    public static Severity fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (Severity severity : values()) {
            if (severity.name().equals(value)) {
                return severity;
            }
        }
        return null;
    }
}
