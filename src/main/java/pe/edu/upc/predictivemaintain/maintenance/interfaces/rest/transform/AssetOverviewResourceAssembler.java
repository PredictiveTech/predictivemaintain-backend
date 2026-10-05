package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetOverview;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AssetOverviewResource;

public final class AssetOverviewResourceAssembler {

    private AssetOverviewResourceAssembler() {
    }

    public static AssetOverviewResource toResource(AssetOverview overview) {
        Asset asset = overview.asset();
        GeoLocation geoLocation = asset.getGeoLocation();
        return new AssetOverviewResource(asset.getId(), asset.getCode(), asset.getName(), asset.getLocation(),
                asset.getPlantId(), asset.getProductionLine(), asset.getAssetType(), asset.getCriticality(),
                geoLocation == null ? null : geoLocation.latitude(),
                geoLocation == null ? null : geoLocation.longitude(),
                asset.isActive(), overview.status(), overview.sensorTypes());
    }
}