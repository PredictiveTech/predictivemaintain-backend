package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Measurement;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.SensorReadingPersistenceEntity;

public final class SensorReadingPersistenceAssembler {

    private SensorReadingPersistenceAssembler() {
    }

    public static SensorReading toDomain(SensorReadingPersistenceEntity entity) {
        return SensorReading.restore(entity.getId(), entity.getTenantId(), entity.getSensorId(),
                entity.getSourceKey(), new Measurement(entity.getValue(), entity.getUnit()),
                entity.getMeasuredAt(), entity.getReceivedAt());
    }

    public static SensorReadingPersistenceEntity toEntity(SensorReading reading) {
        SensorReadingPersistenceEntity entity = new SensorReadingPersistenceEntity();
        entity.setId(reading.getId());
        entity.setTenantId(reading.getTenantId());
        entity.setSensorId(reading.getSensorId());
        entity.setSourceKey(reading.getSourceKey());
        entity.setValue(reading.getValue());
        entity.setUnit(reading.getUnit());
        entity.setMeasuredAt(reading.getMeasuredAt());
        entity.setReceivedAt(reading.getReceivedAt());
        return entity;
    }
}