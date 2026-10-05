package pe.edu.upc.predictivemaintain.subscription.domain.model.commands;

import java.util.UUID;

public record RenewSubscriptionCommand(UUID tenantId) {
}