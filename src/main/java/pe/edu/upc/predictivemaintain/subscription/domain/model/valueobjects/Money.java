package pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Non-negative amount of money with a 3-letter ISO currency.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null || amount.signum() < 0) {
            throw new DomainValidationException("validation.money.amount-negative");
        }
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new DomainValidationException("validation.money.currency-invalid");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }
}