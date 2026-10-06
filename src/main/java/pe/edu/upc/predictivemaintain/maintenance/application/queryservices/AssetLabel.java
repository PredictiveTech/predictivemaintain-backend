package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

/**
 * How a person recognizes an asset: its code and its name.
 */
public record AssetLabel(String code, String name) {
}