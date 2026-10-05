package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

/**
 * One line of the exported history: the event plus the identification of its asset.
 */
public record HistoryRow(String assetCode, String assetName, AssetEvent event) {
}