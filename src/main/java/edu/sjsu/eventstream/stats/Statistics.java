package edu.sjsu.eventstream.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * The numeric rules of the assignment, kept in one place so every
 * aggregator computes them the same way.
 *
 * Median of an even sized list is the average of the two middle
 * values. Standard deviation is the population standard deviation
 * (divide by N, not N-1). Percentiles use the nearest-rank method.
 * Decimal results are rounded to 1 decimal place, and a result that
 * is exactly an integer is returned as a whole number so JSON output
 * writes 40 rather than 40.0.
 */
public final class Statistics {

    private Statistics() {
    }

    public static double mean(List<Double> values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    /** Median of an already sorted list. */
    public static double medianOfSorted(List<Double> sorted) {
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2);
        }
        return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }

    /** Population standard deviation (divide by N). */
    public static double populationStdDev(List<Double> values, double mean) {
        double sum = 0.0;
        for (double value : values) {
            double diff = value - mean;
            sum += diff * diff;
        }
        return Math.sqrt(sum / values.size());
    }

    /**
     * Nearest-rank percentile of an already sorted list: the value at
     * index ceil(p/100 * N) - 1, using exact integer arithmetic.
     */
    public static long percentileNearestRank(List<Long> sorted, int percent) {
        int n = sorted.size();
        long rank = (percent * (long) n + 99) / 100;
        if (rank < 1) {
            rank = 1;
        }
        return sorted.get((int) rank - 1);
    }

    /**
     * Rounds to 1 decimal place (half up). Returns a Long when the
     * rounded value is a whole number, otherwise a Double, so JSON
     * output has no trailing .0 on whole numbers.
     */
    public static Number roundToOneDecimal(double value) {
        return roundToOneDecimal(BigDecimal.valueOf(value));
    }

    public static Number roundToOneDecimal(BigDecimal value) {
        BigDecimal rounded = value
                .setScale(1, RoundingMode.HALF_UP);
        if (rounded.stripTrailingZeros().scale() <= 0) {
            try {
                return rounded.longValueExact();
            } catch (ArithmeticException outsideLongRange) {
                return rounded.toBigIntegerExact();
            }
        }
        return rounded;
    }

    private static final java.math.MathContext PRECISION = java.math.MathContext.DECIMAL128;

    public static BigDecimal decimalMean(List<BigDecimal> values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            sum = sum.add(value);
        }
        return sum.divide(BigDecimal.valueOf(values.size()), PRECISION);
    }

    public static BigDecimal decimalMedian(List<BigDecimal> sorted) {
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2);
        }
        return sorted.get(n / 2 - 1).add(sorted.get(n / 2)).divide(BigDecimal.valueOf(2));
    }

    public static BigDecimal decimalStdDev(List<BigDecimal> values, BigDecimal mean) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            BigDecimal difference = value.subtract(mean);
            sum = sum.add(difference.multiply(difference));
        }
        return sum.divide(BigDecimal.valueOf(values.size()), PRECISION).sqrt(PRECISION);
    }
}
