package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetOverviewsQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

public interface AssetOverviewQueryService {

    PagedResult<AssetOverview> handle(GetAssetOverviewsQuery query);
}