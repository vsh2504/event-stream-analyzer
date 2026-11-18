package edu.sjsu.eventstream.parsing;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyValueLineParserTest {

    private final KeyValueLineParser parser = new KeyValueLineParser();

    @Test
    void parsesSimpleKeyValuePairs() {
        Optional<ParsedLine> parsed = parser.parse(
                "ts=2024-05-10T08:00:00Z sensor=temperature_celsius reading=21.5");
        assertTrue(parsed.isPresent());
        assertEquals("temperature_celsius", parsed.get().get("sensor").orElseThrow());
        assertEquals("21.5", parsed.get().get("reading").orElseThrow());
    }

    @Test
    void parsesQuotedValueWithSpacesAsOneValue() {
        Optional<ParsedLine> parsed = parser.parse(
                "ts=2024-05-10T08:00:05Z severity=INFO device=thermostat-01 msg=\"Heartbeat OK\"");
        assertTrue(parsed.isPresent());
        assertEquals("Heartbeat OK", parsed.get().get("msg").orElseThrow());
        assertEquals("thermostat-01", parsed.get().get("device").orElseThrow());
    }

    @Test
    void parsesQuotedValueContainingEqualsSign() {
        Optional<ParsedLine> parsed = parser.parse("ts=now msg=\"status=fine today\"");
        assertTrue(parsed.isPresent());
        assertEquals("status=fine today", parsed.get().get("msg").orElseThrow());
    }

    @Test
    void keepsExtraKeys() {
        Optional<ParsedLine> parsed = parser.parse(
                "ts=now severity=ERROR device=door-lock-07 code=E41 floor=3");
        assertTrue(parsed.isPresent());
        assertEquals("E41", parsed.get().get("code").orElseThrow());
        assertEquals("3", parsed.get().get("floor").orElseThrow());
    }

    @Test
    void corruptedTokenMakesLineInvalid() {
        assertTrue(parser.parse("ts=2024-05-10T08:00:50Z ###CORRUPTED LINE### !!!").isEmpty());
    }

    @Test
    void unterminatedQuoteMakesLineInvalid() {
        assertTrue(parser.parse("ts=now msg=\"never closed").isEmpty());
    }

    @Test
    void blankLineIsInvalid() {
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse("   ").isEmpty());
    }

    @Test
    void multipleSpacesBetweenTokensAreTolerated() {
        Optional<ParsedLine> parsed = parser.parse(
                "ts=now   sensor=temperature_celsius    reading=21.5");
        assertTrue(parsed.isPresent());
        assertEquals("21.5", parsed.get().get("reading").orElseThrow());
    }

    @Test
    void crlfLineEndingDoesNotPolluteLastValue() {
        Optional<ParsedLine> parsed = parser.parse(
                "ts=now device=thermostat-01 severity=WARN\r");
        assertTrue(parsed.isPresent());
        assertEquals("WARN", parsed.get().get("severity").orElseThrow());
    }

    @Test
    void emptyValueIsStoredButBlank() {
        Optional<ParsedLine> parsed = parser.parse("ts=now sensor= reading=21.5");
        assertTrue(parsed.isPresent());
        assertTrue(parsed.get().getNonBlank("sensor").isEmpty());
    }
}
