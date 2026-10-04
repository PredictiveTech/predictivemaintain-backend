package pe.edu.upc.predictivemaintain.shared.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainConflictException;

/**
 * Aggregate with a version number (optimistic concurrency). The database increments the version on every
 * change; a client can send the version it saw, and if someone else changed the record in the meantime,
 * the request fails instead of silently overwriting their work.
 */
public abstract class AbstractVersionedAggregateRoot extends AbstractDomainAggregateRoot {

    private final long version;

    protected AbstractVersionedAggregateRoot(long version) {
        this.version = version;
    }

    public long getVersion() {
        return version;
    }

    /** A null expectedVersion means "do not check". */
    public void assertExpectedVersion(Long expectedVersion) {
        if (expectedVersion != null && expectedVersion != version) {
            throw new DomainConflictException("conflict.version-mismatch", String.valueOf(version));
        }
    }
}