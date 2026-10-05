package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AssetStatus;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.Criticality;

import java.util.List;
import java.util.UUID;

/**
 * An asset as shown in the list: its data plus the computed status and the variables its sensors measure.
 */
public record AssetOverviewResource(UUID id, String code, String name, String location, UUID plantId,
                                    String productionLine, String assetType, Criticality criticality,
                                    Double latitude, Double longitude, boolean active,
                                    AssetStatus status, List<String> sensorTypes) {
}