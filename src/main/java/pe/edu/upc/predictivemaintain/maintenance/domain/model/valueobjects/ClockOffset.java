package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * The difference between the clock of a device and the clock of the server.
 *
 * <p>A phone may be minutes (or hours) off. When it sends its offline actions it also says what time it is
 * on its own clock; the difference with the server's clock tells us how to translate every timestamp of the
 * batch to the server's timeline. That way "which change is more recent?" compares times from ONE clock.
 */
public record ClockOffset(Duration value) {

    private static final Duration MAX_SKEW = Duration.ofHours(24);

    public ClockOffset {
        Objects.requireNonNull(value);
        if (value.abs().compareTo(MAX_SKEW) > 0) {
            throw new DomainValidationException("validation.sync.clock-skew-too-large");
        }
    }

    /** Positive when the device clock is behind the server's. */
    public static ClockOffset between(Instant deviceSentAt, Instant serverReceivedAt) {
        return new ClockOffset(Duration.between(deviceSentAt, serverReceivedAt));
    }

    /** Translates a device time to server time. The result is never in the future of the server. */
    public Instant toServerTime(Instant deviceTime, Instant serverNow) {
        Instant shifted = deviceTime.plus(value);
        return shifted.isAfter(serverNow) ? serverNow : shifted;
    }

    public long seconds() {
        return value.getSeconds();
    }
}