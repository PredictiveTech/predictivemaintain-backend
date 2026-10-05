package pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Production data of an asset for one period: planned and operating time, units produced and good
 * units, and the ideal seconds per unit. It is the source of the three OEE components.
 * Impossible data is rejected: it would make the OEE meaningless.
 */
public class ProductionWindow extends AbstractDomainAggregateRoot {

    private static final Duration MAX_LENGTH = Duration.ofDays(366);
    private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);

    private final UUID id;
    private final UUID tenantId;
    private final UUID assetId;
    private final Instant startsAt;
    private final Instant endsAt;
    private final long plannedSeconds;
    private final long operatingSeconds;
    private final long totalUnits;
    private final long goodUnits;
    private final BigDecimal idealCycleSeconds;

    private ProductionWindow(UUID id, UUID tenantId, UUID assetId, Instant startsAt, Instant endsAt,
                             long plannedSeconds, long operatingSeconds, long totalUnits, long goodUnits,
                             BigDecimal idealCycleSeconds) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.assetId = Objects.requireNonNull(assetId);
        this.startsAt = Objects.requireNonNull(startsAt);
        this.endsAt = Objects.requireNonNull(endsAt);
        this.plannedSeconds = plannedSeconds;
        this.operatingSeconds = operatingSeconds;
        this.totalUnits = totalUnits;
        this.goodUnits = goodUnits;
        this.idealCycleSeconds = Objects.requireNonNull(idealCycleSeconds);
    }

    public static ProductionWindow record(UUID tenantId, UUID assetId, Instant startsAt, Instant endsAt,
                                          long plannedSeconds, long operatingSeconds, long totalUnits,
                                          long goodUnits, BigDecimal idealCycleSeconds, Instant now) {
        if (startsAt == null || endsAt == null || !startsAt.isBefore(endsAt)
                || Duration.between(startsAt, endsAt).compareTo(MAX_LENGTH) > 0) {
            throw new DomainValidationException("validation.operation-data.range-invalid");
        }
        if (endsAt.isAfter(now.plus(MAX_FUTURE_SKEW))) {
            throw new DomainValidationException("validation.operation-data.in-future");
        }
        long windowSeconds = Duration.between(startsAt, endsAt).getSeconds();
        if (plannedSeconds <= 0 || plannedSeconds > windowSeconds
                || operatingSeconds < 0 || operatingSeconds > plannedSeconds) {
            throw new DomainValidationException("validation.production-window.seconds-invalid");
        }
        if (totalUnits < 0 || goodUnits < 0 || goodUnits > totalUnits) {
            throw new DomainValidationException("validation.production-window.units-invalid");
        }
        if (idealCycleSeconds == null || idealCycleSeconds.signum() <= 0) {
            throw new DomainValidationException("validation.production-window.cycle-invalid");
        }
        BigDecimal cycle = idealCycleSeconds.setScale(3, RoundingMode.HALF_UP);
        // Nobody produces faster than the ideal cycle: more time needed than available means bad data.
        if (cycle.multiply(BigDecimal.valueOf(totalUnits)).compareTo(BigDecimal.valueOf(operatingSeconds)) > 0) {
            throw new DomainValidationException("validation.production-window.performance-invalid");
        }
        return new ProductionWindow(UUID.randomUUID(), tenantId, assetId, startsAt, endsAt, plannedSeconds,
                operatingSeconds, totalUnits, goodUnits, cycle);
    }

    public static ProductionWindow restore(UUID id, UUID tenantId, UUID assetId, Instant startsAt, Instant endsAt,
                                           long plannedSeconds, long operatingSeconds, long totalUnits,
                                           long goodUnits, BigDecimal idealCycleSeconds) {
        return new ProductionWindow(id, tenantId, assetId, startsAt, endsAt, plannedSeconds, operatingSeconds,
                totalUnits, goodUnits, idealCycleSeconds);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public long getPlannedSeconds() {
        return plannedSeconds;
    }

    public long getOperatingSeconds() {
        return operatingSeconds;
    }

    public long getTotalUnits() {
        return totalUnits;
    }

    public long getGoodUnits() {
        return goodUnits;
    }

    public BigDecimal getIdealCycleSeconds() {
        return idealCycleSeconds;
    }
}