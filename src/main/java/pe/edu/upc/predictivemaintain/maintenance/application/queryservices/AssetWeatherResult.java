package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.application.outboundservices.WeatherObservation;

/**
 * Answer of the weather query. The availability explains why there may be no data.
 */
public record AssetWeatherResult(Availability availability, WeatherObservation observation) {

    public enum Availability {
        AVAILABLE,
        UNAVAILABLE,
        NO_LOCATION
    }

    public static AssetWeatherResult available(WeatherObservation observation) {
        return new AssetWeatherResult(Availability.AVAILABLE, observation);
    }

    public static AssetWeatherResult unavailable() {
        return new AssetWeatherResult(Availability.UNAVAILABLE, null);
    }

    public static AssetWeatherResult noLocation() {
        return new AssetWeatherResult(Availability.NO_LOCATION, null);
    }
}