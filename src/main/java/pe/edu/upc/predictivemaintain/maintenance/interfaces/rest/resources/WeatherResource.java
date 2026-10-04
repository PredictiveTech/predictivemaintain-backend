package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import java.time.Instant;

/**
 * @param availability AVAILABLE, UNAVAILABLE (the weather service failed) or NO_LOCATION (the asset has no coordinates)
 */
public record WeatherResource(Double temperature, Double humidity, Instant observedAt, String availability) {
}