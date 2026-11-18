package edu.sjsu.eventstream.aggregate;

import edu.sjsu.eventstream.model.EventRecord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Routes each classified record to the aggregator registered for its
 * type. The registry replaces any if/else dispatch: a record type is
 * supported exactly when an aggregator is registered for it.
 */
public final class AggregationEngine {

    private final Map<Class<?>, Aggregator<?>> aggregatorsByType = new LinkedHashMap<>();
    private final List<Aggregator<?>> aggregators = new ArrayList<>();

    public void register(Aggregator<?> aggregator) {
        aggregatorsByType.put(aggregator.recordType(), aggregator);
        aggregators.add(aggregator);
    }

    @SuppressWarnings("unchecked")
    public void accept(EventRecord record) {
        Aggregator<EventRecord> aggregator =
                (Aggregator<EventRecord>) aggregatorsByType.get(record.getClass());
        if (aggregator != null) {
            aggregator.accept(record);
        }
    }

    /** One report per registered aggregator, in registration order. */
    public List<ReportFile> buildReports() {
        List<ReportFile> reports = new ArrayList<>();
        for (Aggregator<?> aggregator : aggregators) {
            reports.add(new ReportFile(aggregator.reportName(), aggregator.buildReport()));
        }
        return reports;
    }
}
