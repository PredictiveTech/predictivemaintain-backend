package pe.edu.upc.predictivemaintain.telemetry.domain.model.commands;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Metric;

import java.util.UUID;

public record RegisterSensorCommand(UUID tenantId, UUID assetId, Metric metric, String unit) {
}