package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import java.time.Duration;
import java.time.Instant;

/**
 * The time range of a report. It starts before it ends, lasts at most 366 days, and never goes
 * past the present moment.
 */
public record ReportPeriod(Instant from, Instant to) {

    private static final Duration MAX_LENGTH = Duration.ofDays(366);

    public ReportPeriod {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new DomainValidationException("validation.report.range-invalid");
        }
        if (Duration.between(from, to).compareTo(MAX_LENGTH) > 0) {
            throw new DomainValidationException("validation.report.range-too-large");
        }
    }

    /** Builds the period capping the end at "now": a report cannot include time that has not happened yet. */
    public static ReportPeriod until(Instant from, Instant to, Instant now) {
        Instant effectiveEnd = to == null || to.isAfter(now) ? now : to;
        return new ReportPeriod(from, effectiveEnd);
    }

    public long seconds() {
        return Duration.between(from, to).getSeconds();
    }
}