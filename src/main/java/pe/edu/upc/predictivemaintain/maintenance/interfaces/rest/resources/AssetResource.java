package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.Criticality;

import java.util.UUID;

public record AssetResource(UUID id, String code, String name, String location, UUID plantId,
                            String productionLine, String assetType, Criticality criticality,
                            Double latitude, Double longitude, boolean active) {
}