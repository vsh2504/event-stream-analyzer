package edu.sjsu.eventstream.parsing;

import java.util.Optional;

/**
 * Parses one raw input line into key and value pairs. This interface is
 * the seam where a new input format plugs in: a JSON Lines parser, for
 * example, would implement this same interface and produce the same
 * ParsedLine objects, so nothing downstream would change.
 */
public interface LineParser {

    /**
     * Parses a line. Returns empty when the line is blank or corrupted,
     * which tells the caller to skip the line silently.
     */
    Optional<ParsedLine> parse(String line);
}
