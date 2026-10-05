package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Copy of the data that originated an alert: which variable, what value, and which limits it exceeded.
 * It is a snapshot: it never changes, even if the threshold is modified later.
 */
public record AlertDiagnostic(String metric, String unit, BigDecimal observedValue,
                              BigDecimal lowerBound, BigDecimal upperBound, Instant measuredAt) {

    public AlertDiagnostic {
        Objects.requireNonNull(metric);
        Objects.requireNonNull(unit);
        Objects.requireNonNull(observedValue);
        Objects.requireNonNull(lowerBound);
        Objects.requireNonNull(upperBound);
        Objects.requireNonNull(measuredAt);
    }
}