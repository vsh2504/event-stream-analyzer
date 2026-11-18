package edu.sjsu.eventstream.classify;

import edu.sjsu.eventstream.model.AccessRecord;
import edu.sjsu.eventstream.model.EventRecord;
import edu.sjsu.eventstream.parsing.ParsedLine;

import java.util.Optional;

/**
 * Recognises access records: a line with all four of user, door,
 * result (GRANTED or DENIED), and duration_ms (a valid integer).
 */
public final class AccessRecordClassifier extends RecordClassifier {

    @Override
    protected Optional<EventRecord> tryClassify(ParsedLine line) {
        Optional<String> user = line.getNonBlank("user");
        Optional<String> door = line.getNonBlank("door");
        Optional<String> result = line.get("result")
                .filter(value -> value.equals("GRANTED") || value.equals("DENIED"));
        Optional<Long> duration = line.getLong("duration_ms");
        if (user.isPresent() && door.isPresent()
                && result.isPresent() && duration.isPresent()) {
            return Optional.of(new AccessRecord(
                    user.get(), door.get(), result.get(), duration.get()));
        }
        return Optional.empty();
    }
}
