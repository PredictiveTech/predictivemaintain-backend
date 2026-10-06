package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAlertLabelsQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetLabelsQuery;

import java.util.Map;
import java.util.UUID;

/**
 * Looks up, in one go, the labels that make lists readable. A list of 20 alerts needs 1 query, not 20.
 * Ids that do not exist (or belong to another company) are simply missing from the answer.
 */
public interface LabelQueryService {

    Map<UUID, AssetLabel> handle(GetAssetLabelsQuery query);

    Map<UUID, AlertLabel> handle(GetAlertLabelsQuery query);
}