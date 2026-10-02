package pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.Money;

import java.util.Objects;
import java.util.UUID;

/**
 * Commercial offer (Basic, Pro, Enterprise) with an asset limit and a price.
 */
public class SubscriptionPlan extends AbstractDomainAggregateRoot {

    private final UUID id;
    private final String name;
    private int assetLimit;
    private final Money price;

    private SubscriptionPlan(UUID id, String name, int assetLimit, Money price) {
        if (name == null || name.isBlank() || name.length() > 60) {
            throw new DomainValidationException("validation.plan.name-invalid");
        }
        validateLimit(assetLimit);
        this.id = Objects.requireNonNull(id);
        this.name = name.trim();
        this.assetLimit = assetLimit;
        this.price = Objects.requireNonNull(price);
    }

    /** Creates a brand new plan with a generated id. */
    public static SubscriptionPlan create(String name, int assetLimit, Money price) {
        return new SubscriptionPlan(UUID.randomUUID(), name, assetLimit, price);
    }

    /** Rebuilds a plan that already exists in the database. */
    public static SubscriptionPlan restore(UUID id, String name, int assetLimit, Money price) {
        return new SubscriptionPlan(id, name, assetLimit, price);
    }

    public void changeLimit(int newLimit) {
        validateLimit(newLimit);
        this.assetLimit = newLimit;
    }

    private static void validateLimit(int limit) {
        if (limit <= 0) {
            throw new DomainValidationException("validation.plan.limit-invalid");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAssetLimit() {
        return assetLimit;
    }

    public Money getPrice() {
        return price;
    }
}