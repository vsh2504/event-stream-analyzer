package edu.sjsu.eventstream.parsing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parses the key=value line format. Tokens are separated by spaces,
 * except that a value wrapped in double quotes may itself contain
 * spaces (for example msg="Heartbeat OK"). Escaped quotes inside a
 * quoted value are not supported, per the assignment.
 *
 * A line is corrupted and skipped when any token is not a key=value
 * pair, when a key is empty, or when a quote is left unterminated.
 */
public final class KeyValueLineParser implements LineParser {

    @Override
    public Optional<ParsedLine> parse(String line) {
        if (line == null) {
            return Optional.empty();
        }
        // Tolerate CRLF input files: a trailing carriage return is a
        // line ending artifact, never part of the last value.
        if (line.endsWith("\r")) {
            line = line.substring(0, line.length() - 1);
        }
        if (line.isBlank()) {
            return Optional.empty();
        }
        List<String> tokens = tokenize(line);
        if (tokens == null) {
            return Optional.empty();
        }
        Map<String, String> fields = new LinkedHashMap<>();
        for (String token : tokens) {
            int eq = token.indexOf('=');
            if (eq <= 0) {
                return Optional.empty();
            }
            String key = token.substring(0, eq);
            String value = unquote(token.substring(eq + 1));
            fields.put(key, value);
        }
        return Optional.of(new ParsedLine(fields));
    }

    /**
     * Splits a line on spaces that are outside double quotes. Returns
     * null when a quote is never closed.
     */
    private List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if ((c == ' ' || c == '\t') && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (inQuotes) {
            return null;
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    /** Removes the surrounding double quotes of a quoted value. */
    private String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
