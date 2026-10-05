package pe.edu.upc.predictivemaintain.subscription.domain.model.commands;

import java.util.UUID;

public record ChangeSubscriptionPlanCommand(UUID tenantId, UUID planId) {
}