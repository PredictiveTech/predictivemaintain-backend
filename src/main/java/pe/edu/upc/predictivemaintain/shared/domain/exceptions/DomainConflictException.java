package pe.edu.upc.predictivemaintain.shared.domain.exceptions;

/**
 * Thrown when an action is not possible in the current state of the data (409 Conflict).
 * Like DomainValidationException, it carries a message key instead of a text.
 */
public class DomainConflictException extends RuntimeException {

    private final String messageKey;
    private final Object[] arguments;

    public DomainConflictException(String messageKey, Object... arguments) {
        super(messageKey);
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArguments() {
        return arguments;
    }
}