package pe.edu.upc.predictivemaintain.telemetry.application.queryservices;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetAnalyticsQuery;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetRulQuery;

public interface AnalyticsQueryService {

    RulResult handle(GetAssetRulQuery query);

    AssetAnalytics handle(GetAssetAnalyticsQuery query);
}