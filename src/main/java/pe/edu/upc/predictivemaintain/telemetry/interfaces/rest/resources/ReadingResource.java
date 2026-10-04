package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Metric;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReadingResource(UUID id, UUID sensorId, Metric metric, String unit, BigDecimal value,
                              Instant measuredAt, Instant receivedAt) {
}