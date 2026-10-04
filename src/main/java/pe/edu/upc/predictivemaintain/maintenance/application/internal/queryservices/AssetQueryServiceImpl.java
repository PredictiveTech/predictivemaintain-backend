package pe.edu.upc.predictivemaintain.maintenance.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.application.errors.MaintenanceError;
import pe.edu.upc.predictivemaintain.maintenance.application.outboundservices.WeatherProvider;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetQueryService;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetWeatherResult;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAllAssetsQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetByIdQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetWeatherQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

@Service
public class AssetQueryServiceImpl implements AssetQueryService {

    private final AssetRepository assetRepository;
    private final WeatherProvider weatherProvider;

    public AssetQueryServiceImpl(AssetRepository assetRepository, WeatherProvider weatherProvider) {
        this.assetRepository = assetRepository;
        this.weatherProvider = weatherProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public Asset handle(GetAssetByIdQuery query) {
        return assetRepository.findByIdAndTenantId(query.assetId(), query.tenantId())
                .orElseThrow(() -> new ApplicationException(MaintenanceError.ASSET_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Asset> handle(GetAllAssetsQuery query) {
        return assetRepository.search(query.tenantId(), blankToNull(query.productionLine()),
                blankToNull(query.assetType()), query.includeInactive(), query.page());
    }

    /**
     * Deliberately NOT transactional: the call to the external service can take seconds and
     * must not keep a database connection busy while it waits.
     */
    @Override
    public AssetWeatherResult handle(GetAssetWeatherQuery query) {
        Asset asset = assetRepository.findByIdAndTenantId(query.assetId(), query.tenantId())
                .orElseThrow(() -> new ApplicationException(MaintenanceError.ASSET_NOT_FOUND));
        GeoLocation geoLocation = asset.getGeoLocation();
        if (geoLocation == null) {
            return AssetWeatherResult.noLocation();
        }
        return weatherProvider.currentWeather(geoLocation.latitude(), geoLocation.longitude())
                .map(AssetWeatherResult::available)
                .orElseGet(AssetWeatherResult::unavailable);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}