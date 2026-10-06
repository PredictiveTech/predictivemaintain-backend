package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;

import java.util.UUID;

/**
 * What an alert is about: the asset it was raised for (with its label) and how severe it is.
 */
public record AlertLabel(UUID assetId, String assetCode, String assetName, AlertSeverity severity) {
}