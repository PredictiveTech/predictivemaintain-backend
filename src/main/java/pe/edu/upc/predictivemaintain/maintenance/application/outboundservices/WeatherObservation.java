package pe.edu.upc.predictivemaintain.maintenance.application.outboundservices;

import java.time.Instant;

public record WeatherObservation(double temperatureCelsius, double relativeHumidityPercent, Instant observedAt) {
}