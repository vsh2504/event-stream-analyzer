package edu.sjsu.eventstream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyzerApplicationTest {

    @TempDir
    Path tempDir;

    private int run(String... args) {
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errBuffer, true, StandardCharsets.UTF_8);
        return new AnalyzerApplication().run(args, System.out, err);
    }

    private JsonObject readJson(Path file) throws Exception {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    @Test
    void missingFileArgumentIsAnError() {
        assertNotEquals(0, run());
        assertNotEquals(0, run("--outdir", "results"));
    }

    @Test
    void nonexistentInputFileIsAnError() {
        assertNotEquals(0, run("--file", tempDir.resolve("nope.txt").toString()));
    }

    @Test
    void unknownArgumentIsAnError() throws Exception {
        Path input = tempDir.resolve("events.txt");
        Files.writeString(input, "");
        assertNotEquals(0, run("--file", input.toString(), "--bogus", "x"));
    }

    @Test
    void emptyInputProducesThreeEmptyJsonFiles() throws Exception {
        Path input = tempDir.resolve("events.txt");
        Files.writeString(input, "");
        Path outdir = tempDir.resolve("results");

        assertEquals(0, run("--file", input.toString(), "--outdir", outdir.toString()));
        for (String name : new String[] {"sensors.json", "devices.json", "access.json"}) {
            Path file = outdir.resolve(name);
            assertTrue(Files.exists(file), name + " should exist");
            assertEquals("{}", Files.readString(file).trim());
        }
    }

    @Test
    void fileWithOnlyCorruptedLinesProducesEmptyReports() throws Exception {
        Path input = tempDir.resolve("events.txt");
        Files.writeString(input, String.join("\n",
                "ts=2024-05-10T08:00:50Z ###CORRUPTED LINE### !!!",
                "ts=now sensor=temperature_celsius reading=abc",
                "ts=now severity=LOUD device=thermostat-01",
                "ts=now user=user-42 door=door-A result=MAYBE duration_ms=30",
                ""));
        Path outdir = tempDir.resolve("results");

        assertEquals(0, run("--file", input.toString(), "--outdir", outdir.toString()));
        for (String name : new String[] {"sensors.json", "devices.json", "access.json"}) {
            assertEquals("{}", Files.readString(outdir.resolve(name)).trim());
        }
    }

    @Test
    void singleRecordTypeLeavesOtherReportsEmpty() throws Exception {
        Path input = tempDir.resolve("events.txt");
        Files.writeString(input,
                "ts=now sensor=temperature_celsius reading=21.5\n");
        Path outdir = tempDir.resolve("results");

        assertEquals(0, run("--file", input.toString(), "--outdir", outdir.toString()));
        assertTrue(readJson(outdir.resolve("sensors.json")).has("temperature_celsius"));
        assertEquals("{}", Files.readString(outdir.resolve("devices.json")).trim());
        assertEquals("{}", Files.readString(outdir.resolve("access.json")).trim());
    }

    @Test
    void nestedOutdirIsCreated() throws Exception {
        Path input = tempDir.resolve("events.txt");
        Files.writeString(input, "");
        Path outdir = tempDir.resolve("a/b/c");

        assertEquals(0, run("--file", input.toString(), "--outdir", outdir.toString()));
        assertTrue(Files.exists(outdir.resolve("sensors.json")));
    }

    @Test
    void sampleInputProducesExpectedReports() throws Exception {
        String sample = """
                ts=2024-05-10T08:00:00Z sensor=temperature_celsius device=thermostat-01 reading=21.5
                ts=2024-05-10T08:00:05Z severity=INFO device=thermostat-01 msg="Heartbeat OK"
                ts=2024-05-10T08:00:10Z user=user-42 door=door-A result=DENIED duration_ms=30
                ts=2024-05-10T08:00:15Z sensor=humidity_percent device=hygro-02 reading=45
                ts=2024-05-10T08:00:20Z severity=ERROR device=door-lock-07 msg="Latch jammed" code=E41
                ts=2024-05-10T08:00:25Z user=user-42 door=door-A result=DENIED duration_ms=28
                ts=2024-05-10T08:00:30Z sensor=temperature_celsius device=thermostat-02 reading=25.0
                ts=2024-05-10T08:00:35Z severity=WARN device=thermostat-01 msg="Temp drifting"
                ts=2024-05-10T08:00:40Z user=user-17 door=door-B result=GRANTED duration_ms=12
                ts=2024-05-10T08:00:45Z user=user-42 door=door-A result=DENIED duration_ms=35
                ts=2024-05-10T08:00:50Z ###CORRUPTED LINE### !!!
                ts=2024-05-10T08:00:55Z sensor=temperature_celsius device=thermostat-01 reading=abc
                ts=2024-05-10T08:01:00Z severity=CRITICAL device=door-lock-07 msg="Tamper detected"
                ts=2024-05-10T08:01:05Z user=user-08 door=door-A result=GRANTED duration_ms=18
                """;
        Path input = tempDir.resolve("events.txt");
        Files.writeString(input, sample);
        Path outdir = tempDir.resolve("results");

        assertEquals(0, run("--file", input.toString(), "--outdir", outdir.toString()));

        JsonObject sensors = readJson(outdir.resolve("sensors.json"));
        JsonObject temperature = sensors.getAsJsonObject("temperature_celsius");
        assertEquals(2, temperature.get("count").getAsInt());
        assertEquals(21.5, temperature.get("minimum").getAsDouble());
        assertEquals(23.3, temperature.get("median").getAsDouble());
        assertEquals(23.3, temperature.get("average").getAsDouble());
        assertEquals("25", temperature.get("maximum").toString());
        assertEquals(1.8, temperature.get("std_dev").getAsDouble());
        JsonObject humidity = sensors.getAsJsonObject("humidity_percent");
        assertEquals(1, humidity.get("count").getAsInt());
        assertEquals(45.0, humidity.get("average").getAsDouble());
        assertEquals("0", humidity.get("std_dev").toString());

        JsonObject devices = readJson(outdir.resolve("devices.json"));
        JsonObject bySeverity = devices.getAsJsonObject("by_severity");
        assertEquals(1, bySeverity.get("CRITICAL").getAsInt());
        assertEquals(1, bySeverity.get("ERROR").getAsInt());
        assertEquals(1, bySeverity.get("WARN").getAsInt());
        assertEquals(1, bySeverity.get("INFO").getAsInt());
        JsonObject byDevice = devices.getAsJsonObject("by_device");
        assertEquals(2, byDevice.getAsJsonObject("thermostat-01").get("total").getAsInt());
        // thermostat-01 has one INFO and one WARN: the tie breaks to WARN
        assertEquals("WARN", byDevice.getAsJsonObject("thermostat-01")
                .get("most_frequent_severity").getAsString());
        // door-lock-07 has one ERROR and one CRITICAL: the tie breaks to CRITICAL
        assertEquals("CRITICAL", byDevice.getAsJsonObject("door-lock-07")
                .get("most_frequent_severity").getAsString());

        JsonObject access = readJson(outdir.resolve("access.json"));
        JsonObject doorA = access.getAsJsonObject("door-A");
        JsonObject times = doorA.getAsJsonObject("decision_times");
        assertEquals(18, times.get("min").getAsInt());
        assertEquals(28, times.get("50_percentile").getAsInt());
        assertEquals(35, times.get("90_percentile").getAsInt());
        assertEquals(35, times.get("99_percentile").getAsInt());
        assertEquals(35, times.get("max").getAsInt());
        assertEquals(1, doorA.getAsJsonObject("results").get("GRANTED").getAsInt());
        assertEquals(3, doorA.getAsJsonObject("results").get("DENIED").getAsInt());
        assertEquals("user-42",
                doorA.getAsJsonArray("flagged_users").get(0).getAsString());
        JsonObject doorB = access.getAsJsonObject("door-B");
        assertEquals(12, doorB.getAsJsonObject("decision_times").get("max").getAsInt());
        assertEquals(0, doorB.getAsJsonObject("results").get("DENIED").getAsInt());
    }
}
