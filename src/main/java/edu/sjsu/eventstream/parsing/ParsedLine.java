package edu.sjsu.eventstream.parsing;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * One parsed input line: an immutable set of key and value pairs.
 * Classifiers ask this object for the keys they need, including typed
 * lookups that fail quietly when a value has the wrong shape.
 */
public final class ParsedLine {

    private final Map<String, String> fields;

    public ParsedLine(Map<String, String> fields) {
        this.fields = Collections.unmodifiableMap(fields);
    }

    public Map<String, String> fields() {
        return fields;
    }

    /** Raw value for a key, if the key is present at all. */
    public Optional<String> get(String key) {
        return Optional.ofNullable(fields.get(key));
    }

    /** Value for a key, requiring it to be present and non-blank. */
    public Optional<String> getNonBlank(String key) {
        return get(key).filter(value -> !value.isBlank());
    }

    /**
     * Value parsed as a finite double. Empty when the key is missing or
     * the value is not numeric (for example reading=abc) or not finite.
     */
    public Optional<Double> getDouble(String key) {
        return get(key).flatMap(value -> {
            try {
                double parsed = Double.parseDouble(value.trim());
                return Double.isFinite(parsed) ? Optional.of(parsed) : Optional.empty();
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });
    }

    /** Value parsed as an integer. Empty when missing or not an integer. */
    public Optional<Long> getLong(String key) {
        return get(key).flatMap(value -> {
            try {
                return Optional.of(Long.parseLong(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });
    }
}
