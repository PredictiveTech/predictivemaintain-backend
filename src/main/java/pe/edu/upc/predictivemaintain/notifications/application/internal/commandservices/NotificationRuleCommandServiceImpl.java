package pe.edu.upc.predictivemaintain.notifications.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.predictivemaintain.notifications.application.commandservices.NotificationRuleCommandService;
import pe.edu.upc.predictivemaintain.notifications.application.errors.NotificationError;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationRule;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.CreateNotificationRuleCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.DeleteNotificationRuleCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.NotificationRuleRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;

@Service
public class NotificationRuleCommandServiceImpl implements NotificationRuleCommandService {

    private final NotificationRuleRepository ruleRepository;
    private final IamContextFacade iamContextFacade;

    public NotificationRuleCommandServiceImpl(NotificationRuleRepository ruleRepository,
                                              IamContextFacade iamContextFacade) {
        this.ruleRepository = ruleRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Transactional
    public NotificationRule handle(CreateNotificationRuleCommand command) {
        // The recipient must be an active person of the same company.
        if (iamContextFacade.findActiveUser(command.tenantId(), command.userId()).isEmpty()) {
            throw new ApplicationException(NotificationError.RECIPIENT_NOT_VALID);
        }
        NotificationRule rule = NotificationRule.create(command.tenantId(), command.userId(),
                command.assetType(), command.channel());
        if (ruleRepository.exists(command.tenantId(), command.userId(), rule.getAssetType(), rule.getChannel())) {
            throw new ApplicationException(NotificationError.RULE_ALREADY_EXISTS);
        }
        return ruleRepository.save(rule);
    }

    @Override
    @Transactional
    public void handle(DeleteNotificationRuleCommand command) {
        NotificationRule rule = ruleRepository.findByIdAndTenantId(command.ruleId(), command.tenantId())
                .orElseThrow(() -> new ApplicationException(NotificationError.RULE_NOT_FOUND));
        ruleRepository.delete(rule.getId());
    }
}