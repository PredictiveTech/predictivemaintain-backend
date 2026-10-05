package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.Criticality;

import java.util.UUID;

public record RegisterAssetCommand(UUID tenantId, String code, String name, String location, UUID plantId,
                                   String productionLine, String assetType, Criticality criticality,
                                   Double latitude, Double longitude) {
}