package edu.sjsu.eventstream.classify;

import edu.sjsu.eventstream.model.DeviceEvent;
import edu.sjsu.eventstream.model.EventRecord;
import edu.sjsu.eventstream.model.Severity;
import edu.sjsu.eventstream.parsing.ParsedLine;

import java.util.Optional;

/**
 * Recognises device events: a line with a device key and a severity
 * key whose value is one of CRITICAL, ERROR, WARN, or INFO.
 */
public final class DeviceEventClassifier extends RecordClassifier {

    @Override
    protected Optional<EventRecord> tryClassify(ParsedLine line) {
        Optional<String> device = line.getNonBlank("device");
        Severity severity = line.get("severity")
                .map(Severity::fromValue)
                .orElse(null);
        if (device.isPresent() && severity != null) {
            return Optional.of(new DeviceEvent(device.get(), severity));
        }
        return Optional.empty();
    }
}
