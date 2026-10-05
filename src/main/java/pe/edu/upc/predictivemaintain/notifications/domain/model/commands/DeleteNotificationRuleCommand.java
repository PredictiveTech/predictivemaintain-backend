package pe.edu.upc.predictivemaintain.notifications.domain.model.commands;

import java.util.UUID;

public record DeleteNotificationRuleCommand(UUID tenantId, UUID ruleId) {
}