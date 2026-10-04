package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AssetResource;

public final class AssetResourceFromEntityAssembler {

    private AssetResourceFromEntityAssembler() {
    }

    public static AssetResource toResource(Asset asset) {
        GeoLocation geoLocation = asset.getGeoLocation();
        return new AssetResource(
                asset.getId(),
                asset.getCode(),
                asset.getName(),
                asset.getLocation(),
                asset.getPlantId(),
                asset.getProductionLine(),
                asset.getAssetType(),
                asset.getCriticality(),
                geoLocation == null ? null : geoLocation.latitude(),
                geoLocation == null ? null : geoLocation.longitude(),
                asset.isActive());
    }
}