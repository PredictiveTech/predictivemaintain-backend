package pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainConflictException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.InvoiceStatus;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.Money;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Internal billing record of a subscription. It keeps the amount and currency of the moment it was issued:
 * a later change of the plan's price never rewrites an old invoice. It is not a tax document and not a payment.
 */
public class Invoice extends AbstractDomainAggregateRoot {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final UUID id;
    private final UUID tenantId;
    private final UUID subscriptionId;
    private final String number;
    private final String concept;
    private final Money total;
    private InvoiceStatus status;
    private final Instant issuedAt;
    private String paymentReference;

    private Invoice(UUID id, UUID tenantId, UUID subscriptionId, String number, String concept, Money total,
                    InvoiceStatus status, Instant issuedAt, String paymentReference) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.subscriptionId = Objects.requireNonNull(subscriptionId);
        this.number = Objects.requireNonNull(number);
        this.concept = Objects.requireNonNull(concept);
        this.total = Objects.requireNonNull(total);
        this.status = Objects.requireNonNull(status);
        this.issuedAt = Objects.requireNonNull(issuedAt);
        this.paymentReference = paymentReference;
    }

    /**
     * Issues a new invoice. The number combines the day and the start of the invoice id, so it is unique
     * without needing a counter that two simultaneous requests could repeat.
     */
    public static Invoice issue(UUID tenantId, UUID subscriptionId, String concept, Money total, Instant issuedAt) {
        if (concept == null || concept.isBlank() || concept.length() > 120) {
            throw new DomainValidationException("validation.invoice.concept-invalid");
        }
        UUID id = UUID.randomUUID();
        String number = "INV-" + DAY.format(issuedAt) + "-" + id.toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return new Invoice(id, tenantId, subscriptionId, number, concept.trim(), total, InvoiceStatus.ISSUED,
                issuedAt, null);
    }

    public static Invoice restore(UUID id, UUID tenantId, UUID subscriptionId, String number, String concept,
                                  Money total, InvoiceStatus status, Instant issuedAt, String paymentReference) {
        return new Invoice(id, tenantId, subscriptionId, number, concept, total, status, issuedAt, paymentReference);
    }

    /** Only an ISSUED invoice can be paid, and only with a verified external reference. */
    public void markPaid(String reference) {
        requireIssued(InvoiceStatus.PAID);
        if (reference == null || reference.isBlank() || reference.trim().length() > 120) {
            throw new DomainValidationException("validation.invoice.payment-reference-required");
        }
        this.status = InvoiceStatus.PAID;
        this.paymentReference = reference.trim();
    }

    public void markVoid() {
        requireIssued(InvoiceStatus.VOID);
        this.status = InvoiceStatus.VOID;
    }

    private void requireIssued(InvoiceStatus target) {
        if (status != InvoiceStatus.ISSUED) {
            throw new DomainConflictException("conflict.invoice.invalid-transition", status.name(), target.name());
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public String getNumber() {
        return number;
    }

    public String getConcept() {
        return concept;
    }

    public Money getTotal() {
        return total;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public String getPaymentReference() {
        return paymentReference;
    }
}