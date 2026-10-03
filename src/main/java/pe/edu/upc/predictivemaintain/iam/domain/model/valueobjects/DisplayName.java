package pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

/**
 * Visible name of a user: between 1 and 120 characters, without control characters.
 */
public record DisplayName(String value) {

    public DisplayName {
        if (value == null) {
            throw new DomainValidationException("validation.display-name.invalid");
        }
        value = value.trim();
        if (value.isEmpty() || value.length() > 120 || value.chars().anyMatch(Character::isISOControl)) {
            throw new DomainValidationException("validation.display-name.invalid");
        }
    }
}