package pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A period during which an asset was stopped. It is recorded explicitly: a sensor that stops
 * transmitting does not mean the machine stopped.
 */
public class DowntimeInterval extends AbstractDomainAggregateRoot {

    private static final Duration MAX_LENGTH = Duration.ofDays(366);
    private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);

    private final UUID id;
    private final UUID tenantId;
    private final UUID assetId;
    private final Instant startedAt;
    private final Instant endedAt;

    private DowntimeInterval(UUID id, UUID tenantId, UUID assetId, Instant startedAt, Instant endedAt) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.assetId = Objects.requireNonNull(assetId);
        this.startedAt = Objects.requireNonNull(startedAt);
        this.endedAt = Objects.requireNonNull(endedAt);
    }

    public static DowntimeInterval record(UUID tenantId, UUID assetId, Instant startedAt, Instant endedAt,
                                          Instant now) {
        if (startedAt == null || endedAt == null || !startedAt.isBefore(endedAt)
                || Duration.between(startedAt, endedAt).compareTo(MAX_LENGTH) > 0) {
            throw new DomainValidationException("validation.operation-data.range-invalid");
        }
        if (endedAt.isAfter(now.plus(MAX_FUTURE_SKEW))) {
            throw new DomainValidationException("validation.operation-data.in-future");
        }
        return new DowntimeInterval(UUID.randomUUID(), tenantId, assetId, startedAt, endedAt);
    }

    public static DowntimeInterval restore(UUID id, UUID tenantId, UUID assetId, Instant startedAt,
                                           Instant endedAt) {
        return new DowntimeInterval(id, tenantId, assetId, startedAt, endedAt);
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

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }
}