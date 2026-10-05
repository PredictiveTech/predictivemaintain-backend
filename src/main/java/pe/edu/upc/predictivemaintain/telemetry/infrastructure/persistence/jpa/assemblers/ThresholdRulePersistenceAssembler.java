package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.ThresholdRulePersistenceEntity;

public final class ThresholdRulePersistenceAssembler {

    private ThresholdRulePersistenceAssembler() {
    }

    public static ThresholdRule toDomain(ThresholdRulePersistenceEntity entity) {
        return ThresholdRule.restore(entity.getId(), entity.getTenantId(), entity.getSensorId(),
                entity.getLowerBound(), entity.getUpperBound(), entity.getSeverity(), entity.getRuleVersion());
    }

    public static void copyToEntity(ThresholdRule rule, ThresholdRulePersistenceEntity entity) {
        entity.setId(rule.getId());
        entity.setTenantId(rule.getTenantId());
        entity.setSensorId(rule.getSensorId());
        entity.setLowerBound(rule.getLowerBound());
        entity.setUpperBound(rule.getUpperBound());
        entity.setSeverity(rule.getSeverity());
        entity.setRuleVersion(rule.getVersion());
    }
}