package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;

import java.util.UUID;

/**
 * Development tool only: pretends a sensor reading exceeded its threshold.
 */
public record SimulateAlertResource(@NotNull UUID assetId, @NotNull AlertSeverity severity) {
}