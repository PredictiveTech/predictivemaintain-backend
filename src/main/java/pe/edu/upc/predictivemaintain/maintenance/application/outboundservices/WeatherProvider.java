package pe.edu.upc.predictivemaintain.maintenance.application.outboundservices;

import java.util.Optional;

/**
 * Port to an external weather service. It must never throw: when the service fails it returns empty,
 * so the asset sheet keeps working (US-29, scenario 2).
 */
public interface WeatherProvider {

    Optional<WeatherObservation> currentWeather(double latitude, double longitude);
}