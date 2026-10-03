package pe.edu.upc.predictivemaintain.subscription.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.CapacityReservationCommandService;
import pe.edu.upc.predictivemaintain.subscription.application.errors.SubscriptionError;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.CapacityReservation;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ReleaseCapacityCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ReserveCapacityCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.CapacityReservationRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class CapacityReservationCommandServiceImpl implements CapacityReservationCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final CapacityReservationRepository reservationRepository;
    private final Clock clock;

    public CapacityReservationCommandServiceImpl(SubscriptionRepository subscriptionRepository,
                                                 CapacityReservationRepository reservationRepository,
                                                 Clock clock) {
        this.subscriptionRepository = subscriptionRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UUID handle(ReserveCapacityCommand command) {
        Instant now = clock.instant();

        // Locks the subscription row: concurrent reservations of one company wait for each other,
        // so the limit cannot be exceeded by two simultaneous requests.
        Subscription subscription = subscriptionRepository.findCurrentForUpdate(command.tenantId())
                .filter(candidate -> candidate.isActive(now))
                .orElseThrow(() -> new ApplicationException(SubscriptionError.SUBSCRIPTION_NOT_ACTIVE));

        Optional<CapacityReservation> previous =
                reservationRepository.findByTenantIdAndOperationId(command.tenantId(), command.operationId());
        if (previous.isPresent()) {
            return previous.get().getId();
        }

        long occupied = reservationRepository.countOccupiedBySubscriptionId(subscription.getId());
        if (occupied >= subscription.getAssetLimit()) {
            throw new ApplicationException(SubscriptionError.CAPACITY_EXCEEDED, subscription.getAssetLimit());
        }

        CapacityReservation reservation = CapacityReservation.confirmed(
                command.tenantId(), subscription.getId(), command.operationId(), command.assetId(), now);
        return reservationRepository.save(reservation).getId();
    }

    @Override
    @Transactional
    public void handle(ReleaseCapacityCommand command) {
        reservationRepository.findByIdAndTenantId(command.reservationId(), command.tenantId())
                .ifPresent(reservation -> {
                    reservation.release();
                    reservationRepository.save(reservation);
                });
    }
}