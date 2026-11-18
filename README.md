# Event Stream Analyzer

A command line application that reads a plain text file of IoT and smart building event records, classifies each line as a sensor reading, a device event, or an access record, and writes aggregated results into three JSON files.

## Build

Requires Java 17 or newer and Maven.

```bash
mvn package
```

This runs the test suite and produces `target/analyzer.jar`.

## Run

```bash
java -jar target/analyzer.jar --file events.txt --outdir results/
```

- `--file <filename.txt>` is required. A missing argument or a file that does not exist prints an error and exits with a non-zero code.
- `--outdir <directory>` is optional and defaults to the current directory. The directory is created if it does not exist.

An example input file is in `examples/events.txt`.

## Output

Three files are always written, even when a record type never appears in the input (then the file contains an empty JSON object `{}`). Output is pretty printed with 2-space indentation.

- `sensors.json`: per sensor, the count, minimum, median, average, maximum, and standard deviation of its readings.
- `devices.json`: event counts per severity level, and per device the total count and most frequent severity.
- `access.json`: per door, decision time statistics, GRANTED and DENIED counts, and the users with 3 or more denied attempts on that door.

## Input format

Each line is a sequence of space-separated `key=value` pairs. A value that contains spaces is wrapped in double quotes, for example `msg="Heartbeat OK"`. Escaped quotes inside quoted values are not supported. Every line begins with a `ts` timestamp field, which is parsed but not used in any aggregation. Extra keys are ignored.

A line is classified by the keys it carries:

- **Sensor reading**: a `sensor` key and a `reading` key with a valid numeric value.
- **Device event**: a `severity` key (CRITICAL, ERROR, WARN, or INFO) and a `device` key.
- **Access record**: all of `user`, `door`, `result` (GRANTED or DENIED), and `duration_ms` (a valid integer).

If a line matches more than one type, the first match in the order above wins. A line that matches nothing (missing keys, invalid values, corrupted data) is skipped silently.

## Numeric rules

These rules are stated here so results are reproducible:

- **Median**: for an even number of values, the average of the two middle values; for an odd number, the middle value.
- **Standard deviation**: population standard deviation (divide by N, not N-1).
- **Percentiles** (access decision times): the **nearest-rank method**. Given a sorted list of N values, the p-th percentile is the value at index `ceil(p/100 * N) - 1` (0-indexed). Because of this, the 50th percentile can differ from the median on even sized lists; that is intentional.
- **Rounding**: decimal results are rounded to 1 decimal place. A result that is exactly an integer is written without a decimal point (40, not 40.0).
- **Most frequent severity ties**: when two severity levels tie for a device, the higher severity wins. Severity order is CRITICAL, then ERROR, then WARN, then INFO.

## Design

Parsing, classification, aggregation, and output are separate responsibilities, connected by three design patterns:

- **Chain of Responsibility** for classification: one classifier per record type, linked in a chain (`RecordClassifier` and its subclasses).
- **Strategy** for aggregation and output: one `Aggregator` per record type behind a common interface, and a `ReportWriter` interface for the output format.
- **Factory** for wiring: `AnalyzerFactory` builds the parser, links the classifier chain, and registers the aggregators, in one place.

The full design document, including the UML class diagram and an extensibility analysis (new record types, JSON Lines input, CSV output), is in `docs/design.pdf`. The diagram source is `docs/class-diagram.puml`.

## Tests

```bash
mvn test
```

The suite has 54 JUnit 5 tests covering the parser (quoted values, delimiter edge cases, corrupted lines, CRLF input), the classifier chain (each record type, invalid values, precedence), each aggregator (including tie-breaking, nearest-rank percentiles, decimal precision, and rounding), and the command line application end to end (error exit codes, empty input, and the assignment's sample input with hand-checked expected output).

## Project layout

```
src/main/java/edu/sjsu/eventstream/
  Main.java, AnalyzerApplication.java   command line entry and argument handling
  EventStreamAnalyzer.java              the parse, classify, aggregate pipeline
  AnalyzerFactory.java                  component wiring (Factory)
  parsing/                              line parser and parsed line model
  classify/                             classifier chain (Chain of Responsibility)
  model/                                typed records and the Severity enum
  aggregate/                            aggregators and the routing engine (Strategy)
  report/                               report model types
  stats/                                median, standard deviation, percentile, rounding
  output/                               report writers (Strategy)
docs/                                   design document and class diagram
examples/                               sample input file
```
