package pe.edu.upc.predictivemaintain.shared.application.errors;

/**
 * Contract implemented by the error enum of each bounded context.
 */
public interface ErrorCode {

    /** HTTP status that represents this error. */
    int status();

    /** Key of the localized message in messages*.properties. */
    String messageKey();

    /** Stable code the clients can rely on (the enum constant name). */
    default String code() {
        return this instanceof Enum<?> constant ? constant.name() : getClass().getSimpleName();
    }
}