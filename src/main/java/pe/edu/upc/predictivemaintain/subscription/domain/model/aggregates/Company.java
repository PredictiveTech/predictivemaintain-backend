package pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.util.Objects;
import java.util.UUID;

/**
 * Corporate account. Its id is the tenantId that every other context uses to
 * separate the data of one company from another.
 */
public class Company extends AbstractDomainAggregateRoot {

    private final UUID id;
    private final String name;
    private final UUID registrationId;
    private boolean active;

    private Company(UUID id, String name, UUID registrationId, boolean active) {
        if (name == null || name.isBlank() || name.trim().length() > 150) {
            throw new DomainValidationException("validation.company.name-invalid");
        }
        this.id = Objects.requireNonNull(id);
        this.name = name.trim();
        this.registrationId = Objects.requireNonNull(registrationId);
        this.active = active;
    }

    /**
     * @param registrationId idempotency key sent by the client: repeating a registration
     *                       with the same key does not create a second company
     */
    public static Company create(String name, UUID registrationId) {
        return new Company(UUID.randomUUID(), name, registrationId, true);
    }

    public static Company restore(UUID id, String name, UUID registrationId, boolean active) {
        return new Company(id, name, registrationId, active);
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }

    public boolean isActive() {
        return active;
    }
}