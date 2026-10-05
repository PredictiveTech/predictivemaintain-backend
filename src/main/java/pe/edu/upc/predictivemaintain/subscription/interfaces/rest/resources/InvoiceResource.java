package pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @param paymentReference null until a verified payment is registered; ISSUED does not mean paid
 */
public record InvoiceResource(UUID id, String number, String concept, BigDecimal amount, String currency,
                              InvoiceStatus status, Instant issuedAt, String paymentReference) {
}