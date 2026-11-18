package edu.sjsu.eventstream.classify;

import edu.sjsu.eventstream.model.EventRecord;
import edu.sjsu.eventstream.parsing.ParsedLine;

import java.util.Optional;

/**
 * One link in the classification chain (Chain of Responsibility).
 * Each subclass recognises exactly one record type. When a classifier
 * cannot classify a line it passes the line to the next classifier,
 * and a line that no classifier recognises is skipped silently.
 *
 * The order of the chain defines precedence when a line could match
 * more than one record type: the first match wins.
 */
public abstract class RecordClassifier {

    private RecordClassifier next;

    /** Links the next classifier and returns it, for fluent chaining. */
    public RecordClassifier setNext(RecordClassifier next) {
        this.next = next;
        return next;
    }

    public final Optional<EventRecord> classify(ParsedLine line) {
        Optional<EventRecord> result = tryClassify(line);
        if (result.isPresent()) {
            return result;
        }
        return next != null ? next.classify(line) : Optional.empty();
    }

    /** Returns the record when this classifier recognises the line. */
    protected abstract Optional<EventRecord> tryClassify(ParsedLine line);
}
