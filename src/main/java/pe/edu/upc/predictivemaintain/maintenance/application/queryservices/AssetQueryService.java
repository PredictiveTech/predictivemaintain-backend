package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAllAssetsQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetByIdQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetWeatherQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

public interface AssetQueryService {

    Asset handle(GetAssetByIdQuery query);

    PagedResult<Asset> handle(GetAllAssetsQuery query);

    AssetWeatherResult handle(GetAssetWeatherQuery query);
}