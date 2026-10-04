package pe.edu.upc.predictivemaintain.telemetry.domain.repositories;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThresholdRuleRepository {

    ThresholdRule save(ThresholdRule rule);

    Optional<ThresholdRule> findBySensorId(UUID tenantId, UUID sensorId);

    List<ThresholdRule> findBySensorIds(UUID tenantId, Collection<UUID> sensorIds);
}