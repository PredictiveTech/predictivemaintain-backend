package pe.edu.upc.predictivemaintain.telemetry.domain.repositories;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorRepository {

    Sensor save(Sensor sensor);

    /** Without company: used to authenticate a device, whose company is only known once its sensor is found. */
    Optional<Sensor> findById(UUID id);

    Optional<Sensor> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Sensor> findByAssetId(UUID tenantId, UUID assetId);
}