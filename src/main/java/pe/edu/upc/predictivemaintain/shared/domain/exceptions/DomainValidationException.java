package pe.edu.upc.predictivemaintain.shared.domain.exceptions;

/**
 * Thrown when a business invariant is violated. It carries a message key
 * (not a text) so the API layer can localize it.
 */
public class DomainValidationException extends RuntimeException {

    private final String messageKey;
    private final Object[] arguments;

    public DomainValidationException(String messageKey, Object... arguments) {
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