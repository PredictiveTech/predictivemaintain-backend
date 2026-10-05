package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.RulResult.Availability;

import java.time.Instant;

/**
 * @param availability          UNAVAILABLE means there is not enough history or no clear trend (all other values are null)
 * @param hoursRemaining        estimated hours until the variable reaches its limit
 * @param confidence            between 0 and 1
 * @param interventionRecommended true when the remaining life is below the configured critical value
 */
public record RulResource(Availability availability, Double hoursRemaining, Double confidence,
                          String modelVersion, Instant estimatedAt, String drivingMetric,
                          boolean interventionRecommended) {
}