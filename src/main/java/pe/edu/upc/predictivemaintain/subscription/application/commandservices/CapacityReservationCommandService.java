package pe.edu.upc.predictivemaintain.subscription.application.commandservices;

import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ReleaseCapacityCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ReserveCapacityCommand;

import java.util.UUID;

/**
 * Write use cases for the asset capacity of a subscription.
 */
public interface CapacityReservationCommandService {

    /** Reserves one slot and returns the reservation id. Repeating the operationId returns the same id. */
    UUID handle(ReserveCapacityCommand command);

    /** Releases a slot. Idempotent: an unknown or already released reservation is ignored. */
    void handle(ReleaseCapacityCommand command);
}