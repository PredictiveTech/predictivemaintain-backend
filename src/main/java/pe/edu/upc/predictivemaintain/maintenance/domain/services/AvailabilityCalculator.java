package pe.edu.upc.predictivemaintain.maintenance.domain.services;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.DowntimeInterval;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.ReportPeriod;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Availability = share of the period during which the asset was not stopped.
 * It is a pure calculation: no database, no framework.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {
    }

    /**
     * Seconds of downtime inside the period. Each stop is cut to the period and overlapping stops are
     * merged, so a second in which two stops coincide is counted once.
     */
    public static long downtimeSeconds(ReportPeriod period, List<DowntimeInterval> intervals) {
        List<long[]> clipped = new ArrayList<>();
        for (DowntimeInterval interval : intervals) {
            Instant start = interval.getStartedAt().isBefore(period.from()) ? period.from() : interval.getStartedAt();
            Instant end = interval.getEndedAt().isAfter(period.to()) ? period.to() : interval.getEndedAt();
            if (end.isAfter(start)) {
                clipped.add(new long[]{start.getEpochSecond(), end.getEpochSecond()});
            }
        }
        clipped.sort(Comparator.comparingLong(interval -> interval[0]));

        long total = 0;
        long currentStart = 0;
        long currentEnd = 0;
        boolean open = false;
        for (long[] interval : clipped) {
            if (!open) {
                currentStart = interval[0];
                currentEnd = interval[1];
                open = true;
            } else if (interval[0] <= currentEnd) {
                currentEnd = Math.max(currentEnd, interval[1]);
            } else {
                total += currentEnd - currentStart;
                currentStart = interval[0];
                currentEnd = interval[1];
            }
        }
        if (open) {
            total += currentEnd - currentStart;
        }
        return total;
    }

    /** Percentage with two decimals. With no time in the period there was nothing to lose: 100. */
    public static double availabilityPercent(long periodSeconds, long downtimeSeconds) {
        if (periodSeconds <= 0) {
            return 100.0;
        }
        return round(100.0 * (periodSeconds - downtimeSeconds) / periodSeconds);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}