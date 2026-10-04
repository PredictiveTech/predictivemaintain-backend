package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.ProductionWindow;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.ProductionWindowPersistenceEntity;

public final class ProductionWindowPersistenceAssembler {

    private ProductionWindowPersistenceAssembler() {
    }

    public static ProductionWindow toDomain(ProductionWindowPersistenceEntity entity) {
        return ProductionWindow.restore(entity.getId(), entity.getTenantId(), entity.getAssetId(),
                entity.getStartsAt(), entity.getEndsAt(), entity.getPlannedSeconds(), entity.getOperatingSeconds(),
                entity.getTotalUnits(), entity.getGoodUnits(), entity.getIdealCycleSeconds());
    }

    public static ProductionWindowPersistenceEntity toEntity(ProductionWindow window) {
        ProductionWindowPersistenceEntity entity = new ProductionWindowPersistenceEntity();
        entity.setId(window.getId());
        entity.setTenantId(window.getTenantId());
        entity.setAssetId(window.getAssetId());
        entity.setStartsAt(window.getStartsAt());
        entity.setEndsAt(window.getEndsAt());
        entity.setPlannedSeconds(window.getPlannedSeconds());
        entity.setOperatingSeconds(window.getOperatingSeconds());
        entity.setTotalUnits(window.getTotalUnits());
        entity.setGoodUnits(window.getGoodUnits());
        entity.setIdealCycleSeconds(window.getIdealCycleSeconds());
        return entity;
    }
}