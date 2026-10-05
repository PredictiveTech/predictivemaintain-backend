package pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects;

/**
 * ISSUED: the invoice exists. PAID: only with a verified external payment reference. VOID: cancelled.
 * Issuing an invoice does not prove a payment.
 */
public enum InvoiceStatus {
    ISSUED,
    PAID,
    VOID
}