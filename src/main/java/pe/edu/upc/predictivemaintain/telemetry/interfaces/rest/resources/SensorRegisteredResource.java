package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Metric;

import java.util.UUID;

/**
 * @param deviceKey the sensor's secret key; shown only in this answer. Configure it in the device.
 */
public record SensorRegisteredResource(UUID id, UUID assetId, Metric metric, String unit, boolean active,
                                       @Schema(description = "Shown only once: save it now") String deviceKey) {
}