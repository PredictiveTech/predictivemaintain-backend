package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.AssetPersistenceEntity;

public final class AssetPersistenceAssembler {

    private AssetPersistenceAssembler() {
    }

    public static Asset toDomain(AssetPersistenceEntity entity) {
        GeoLocation geoLocation = entity.getLatitude() != null && entity.getLongitude() != null
                ? new GeoLocation(entity.getLatitude(), entity.getLongitude())
                : null;
        return Asset.restore(entity.getId(), entity.getTenantId(), entity.getCode(), entity.getName(),
                entity.getLocation(), entity.getPlantId(), entity.getProductionLine(), entity.getAssetType(),
                entity.getCriticality(), geoLocation, entity.isActive(), entity.getCapacityReservationId());
    }

    public static void copyToEntity(Asset asset, AssetPersistenceEntity entity) {
        entity.setId(asset.getId());
        entity.setTenantId(asset.getTenantId());
        entity.setCode(asset.getCode());
        entity.setName(asset.getName());
        entity.setLocation(asset.getLocation());
        entity.setPlantId(asset.getPlantId());
        entity.setProductionLine(asset.getProductionLine());
        entity.setAssetType(asset.getAssetType());
        entity.setCriticality(asset.getCriticality());
        entity.setLatitude(asset.getGeoLocation() == null ? null : asset.getGeoLocation().latitude());
        entity.setLongitude(asset.getGeoLocation() == null ? null : asset.getGeoLocation().longitude());
        entity.setActive(asset.isActive());
        entity.setCapacityReservationId(asset.getCapacityReservationId());
    }
}