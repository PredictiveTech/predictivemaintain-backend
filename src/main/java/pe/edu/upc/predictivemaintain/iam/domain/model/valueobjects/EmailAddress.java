package pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Email normalized to lower case. Normalizing here makes the uniqueness check reliable.
 */
public record EmailAddress(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public EmailAddress {
        if (value == null) {
            throw new DomainValidationException("validation.email.invalid");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 254 || !FORMAT.matcher(value).matches()) {
            throw new DomainValidationException("validation.email.invalid");
        }
    }
}