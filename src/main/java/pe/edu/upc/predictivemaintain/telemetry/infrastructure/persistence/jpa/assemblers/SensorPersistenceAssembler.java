package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.SensorPersistenceEntity;

public final class SensorPersistenceAssembler {

    private SensorPersistenceAssembler() {
    }

    public static Sensor toDomain(SensorPersistenceEntity entity) {
        return Sensor.restore(entity.getId(), entity.getTenantId(), entity.getAssetId(), entity.getMetric(),
                entity.getUnit(), entity.isActive(), entity.getLastReceivedAt(), entity.getLastAlertAt(),
                entity.getDeviceKeyHash());
    }

    public static void copyToEntity(Sensor sensor, SensorPersistenceEntity entity) {
        entity.setId(sensor.getId());
        entity.setTenantId(sensor.getTenantId());
        entity.setAssetId(sensor.getAssetId());
        entity.setMetric(sensor.getMetric());
        entity.setUnit(sensor.getUnit());
        entity.setActive(sensor.isActive());
        entity.setLastReceivedAt(sensor.getLastReceivedAt());
        entity.setLastAlertAt(sensor.getLastAlertAt());
        entity.setDeviceKeyHash(sensor.getDeviceKeyHash());
    }
}