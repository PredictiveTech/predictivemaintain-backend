package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * What originated the alert: the variable, the value measured, and the limits it exceeded (US-05).
 */
public record AlertDiagnosticResource(String metric, String unit, BigDecimal observedValue,
                                      BigDecimal lowerBound, BigDecimal upperBound, Instant measuredAt) {
}