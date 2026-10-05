package pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A measured value with its unit. Immutable. The value is kept with 6 decimals and must be a reasonable number.
 */
public record Measurement(BigDecimal value, String unit) {

    private static final BigDecimal MAX_ABSOLUTE_VALUE = new BigDecimal("1000000000000");

    public Measurement {
        if (value == null || value.abs().compareTo(MAX_ABSOLUTE_VALUE) >= 0) {
            throw new DomainValidationException("validation.measurement.value-invalid");
        }
        if (unit == null || unit.isBlank() || unit.trim().length() > 20) {
            throw new DomainValidationException("validation.measurement.unit-invalid");
        }
        value = value.setScale(6, RoundingMode.HALF_UP);
        unit = unit.trim();
    }
}