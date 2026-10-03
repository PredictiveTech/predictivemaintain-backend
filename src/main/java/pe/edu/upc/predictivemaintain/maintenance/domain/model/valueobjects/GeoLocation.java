package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

/**
 * Geographic position of an asset. Latitude and longitude always travel together.
 */
public record GeoLocation(double latitude, double longitude) {

    public GeoLocation {
        // Written as "not inside the range" so NaN is rejected too.
        if (!(latitude >= -90 && latitude <= 90) || !(longitude >= -180 && longitude <= 180)) {
            throw new DomainValidationException("validation.geo-location.invalid");
        }
    }

    /**
     * Returns null when both values are missing, and fails when only one of them is present.
     */
    public static GeoLocation fromNullable(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return null;
        }
        if (latitude == null || longitude == null) {
            throw new DomainValidationException("validation.geo-location.incomplete");
        }
        return new GeoLocation(latitude, longitude);
    }
}