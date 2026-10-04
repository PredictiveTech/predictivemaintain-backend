package pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.Criticality;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Industrial equipment inventoried by a company. The code never changes and is unique inside the company.
 */
public class Asset extends AbstractDomainAggregateRoot {

    private final UUID id;
    private final UUID tenantId;
    private final String code;
    private String name;
    private String location;
    private UUID plantId;
    private String productionLine;
    private String assetType;
    private Criticality criticality;
    private GeoLocation geoLocation;
    private boolean active;
    private final UUID capacityReservationId;

    private Asset(UUID id, UUID tenantId, String code, String name, String location, UUID plantId,
                  String productionLine, String assetType, Criticality criticality, GeoLocation geoLocation,
                  boolean active, UUID capacityReservationId) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.code = Objects.requireNonNull(code);
        this.capacityReservationId = Objects.requireNonNull(capacityReservationId);
        this.active = active;
        applyDetails(name, location, plantId, productionLine, assetType, criticality, geoLocation);
    }

    /** Codes are stored trimmed and in upper case, so PUMP-1 and pump-1 are the same asset. */
    public static String normalizeCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank() || rawCode.trim().length() > 50) {
            throw new DomainValidationException("validation.asset.code-invalid");
        }
        return rawCode.trim().toUpperCase(Locale.ROOT);
    }

    public static Asset register(UUID id, UUID tenantId, String code, String name, String location, UUID plantId,
                                 String productionLine, String assetType, Criticality criticality,
                                 GeoLocation geoLocation, UUID capacityReservationId) {
        return new Asset(id, tenantId, normalizeCode(code), name, location, plantId, productionLine, assetType,
                criticality, geoLocation, true, capacityReservationId);
    }

    public static Asset restore(UUID id, UUID tenantId, String code, String name, String location, UUID plantId,
                                String productionLine, String assetType, Criticality criticality,
                                GeoLocation geoLocation, boolean active, UUID capacityReservationId) {
        return new Asset(id, tenantId, code, name, location, plantId, productionLine, assetType, criticality,
                geoLocation, active, capacityReservationId);
    }

    /** Changes the technical data. The code cannot be changed. */
    public void update(String name, String location, UUID plantId, String productionLine, String assetType,
                       Criticality criticality, GeoLocation geoLocation) {
        applyDetails(name, location, plantId, productionLine, assetType, criticality, geoLocation);
    }

    /** Logical deletion: the asset stays in the database but leaves the monitoring. */
    public void deactivate() {
        this.active = false;
    }

    private void applyDetails(String newName, String newLocation, UUID newPlantId, String newProductionLine,
                              String newAssetType, Criticality newCriticality, GeoLocation newGeoLocation) {
        // Everything is validated first, so a failure leaves the asset untouched.
        String validName = requireText(newName, 120, "validation.asset.name-invalid");
        String validLocation = requireText(newLocation, 120, "validation.asset.location-invalid");
        String validType = requireText(newAssetType, 80, "validation.asset.type-invalid");
        String validLine = optionalText(newProductionLine, 100, "validation.asset.production-line-invalid");
        if (newCriticality == null) {
            throw new DomainValidationException("validation.asset.criticality-required");
        }
        this.name = validName;
        this.location = validLocation;
        this.assetType = validType;
        this.productionLine = validLine;
        this.criticality = newCriticality;
        this.plantId = newPlantId;
        this.geoLocation = newGeoLocation;
    }

    private static String requireText(String value, int maxLength, String errorKey) {
        if (value == null || value.isBlank() || value.trim().length() > maxLength) {
            throw new DomainValidationException(errorKey);
        }
        return value.trim();
    }

    private static String optionalText(String value, int maxLength, String errorKey) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.trim().length() > maxLength) {
            throw new DomainValidationException(errorKey);
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public UUID getPlantId() {
        return plantId;
    }

    public String getProductionLine() {
        return productionLine;
    }

    public String getAssetType() {
        return assetType;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public boolean isActive() {
        return active;
    }

    public UUID getCapacityReservationId() {
        return capacityReservationId;
    }
}