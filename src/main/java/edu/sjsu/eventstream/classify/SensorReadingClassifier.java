package edu.sjsu.eventstream.classify;

import edu.sjsu.eventstream.model.EventRecord;
import edu.sjsu.eventstream.model.SensorReading;
import edu.sjsu.eventstream.parsing.ParsedLine;

import java.util.Optional;

/**
 * Recognises sensor readings: a line with a sensor key and a reading
 * key whose value is a valid number.
 */
public final class SensorReadingClassifier extends RecordClassifier {

    @Override
    protected Optional<EventRecord> tryClassify(ParsedLine line) {
        Optional<String> sensor = line.getNonBlank("sensor");
        Optional<Double> reading = line.getDouble("reading");
        if (sensor.isPresent() && reading.isPresent()) {
            try {
                return Optional.of(new SensorReading(sensor.get(),
                        new java.math.BigDecimal(line.get("reading").orElseThrow().trim())));
            } catch (NumberFormatException invalidDecimal) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
