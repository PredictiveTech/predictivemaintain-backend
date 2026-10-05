package pe.edu.upc.predictivemaintain.notifications.application.commandservices;

import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationRule;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.CreateNotificationRuleCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.DeleteNotificationRuleCommand;

public interface NotificationRuleCommandService {

    NotificationRule handle(CreateNotificationRuleCommand command);

    void handle(DeleteNotificationRuleCommand command);
}