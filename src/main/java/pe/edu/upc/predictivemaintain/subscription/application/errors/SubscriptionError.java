package pe.edu.upc.predictivemaintain.subscription.application.errors;

import pe.edu.upc.predictivemaintain.shared.application.errors.ErrorCode;

/**
 * Expected failures of the Subscription & Billing context.
 */
public enum SubscriptionError implements ErrorCode {

    PLAN_ALREADY_EXISTS(409, "error.subscription.plan-already-exists"),
    PLAN_NOT_FOUND(404, "error.subscription.plan-not-found"),
    SUBSCRIPTION_NOT_FOUND(404, "error.subscription.not-found"),
    SUBSCRIPTION_NOT_ACTIVE(403, "error.subscription.not-active"),
    CAPACITY_EXCEEDED(403, "error.subscription.capacity-exceeded");

    private final int status;
    private final String messageKey;

    SubscriptionError(int status, String messageKey) {
        this.status = status;
        this.messageKey = messageKey;
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }
}