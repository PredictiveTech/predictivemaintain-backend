package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignWorkOrderResource(@NotNull UUID technicianId, Long expectedVersion) {
}