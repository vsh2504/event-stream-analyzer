package edu.sjsu.eventstream.aggregate;

import edu.sjsu.eventstream.model.SensorReading;
import edu.sjsu.eventstream.report.SensorStats;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DecimalRegressionTest {
    private SensorStats stats(String... values) {
        SensorAggregator aggregator = new SensorAggregator();
        for (String value : values) aggregator.accept(new SensorReading("x", new BigDecimal(value)));
        return (SensorStats) ((Map<?, ?>) aggregator.buildReport()).get("x");
    }

    @Test void decimalHalfBoundaryRoundsUp() {
        SensorStats result = stats("2.3", "2.4");
        assertEquals("2.4", result.average().toString());
        assertEquals("2.4", result.median().toString());
    }

    @Test void negativeHalfBoundaryRoundsAwayFromZero() {
        assertEquals("-2.4", stats("-2.3", "-2.4").average().toString());
    }

    @Test void wholeNumbersOutsideLongRangeDoNotWrap() {
        assertEquals("100000000000000000000", stats("1e20").minimum().toString());
    }

    @Test void finiteLargeReadingsDoNotOverflow() {
        SensorStats result = stats("1e308", "1e308");
        assertEquals(0, new BigDecimal(result.average().toString()).compareTo(new BigDecimal("1e308")));
        assertEquals("0", result.stdDev().toString());
    }
}
