package pe.edu.upc.predictivemaintain.notifications.application.internal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.IamContextFacade.UserContact;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.events.AlertRaisedEvent;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.acl.MaintenanceContextFacade;
import pe.edu.upc.predictivemaintain.notifications.application.outboundservices.PushMessage;
import pe.edu.upc.predictivemaintain.notifications.application.outboundservices.PushResult;
import pe.edu.upc.predictivemaintain.notifications.application.outboundservices.PushSender;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.DeviceToken;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationDelivery;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationRule;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.DeliveryStatus;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationType;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.DeviceTokenRepository;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.NotificationDeliveryRepository;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.NotificationRuleRepository;
import pe.edu.upc.predictivemaintain.shared.application.outboundservices.EmailDeliveryException;
import pe.edu.upc.predictivemaintain.shared.application.outboundservices.EmailSender;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Decides WHO to notify, builds the message and delivers it through each channel, leaving a record of every
 * attempt. It is the only place that talks to the push service and the mail server.
 *
 * <p>Two guarantees matter: a notification can never break what triggered it (everything here is caught and
 * recorded, TS-08 scenario 2), and the same event is never notified twice to the same person and channel
 * (the dedupe key).
 */
@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationRuleRepository ruleRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final PushSender pushSender;
    private final EmailSender emailSender;
    private final IamContextFacade iamContextFacade;
    private final MaintenanceContextFacade maintenanceContextFacade;
    private final MessageSource messages;
    private final Clock clock;
    private final Locale locale;

    public NotificationDispatcher(NotificationRuleRepository ruleRepository,
                                  DeviceTokenRepository deviceTokenRepository,
                                  NotificationDeliveryRepository deliveryRepository,
                                  PushSender pushSender,
                                  EmailSender emailSender,
                                  IamContextFacade iamContextFacade,
                                  MaintenanceContextFacade maintenanceContextFacade,
                                  MessageSource messages,
                                  Clock clock,
                                  @Value("${app.notifications.locale:es}") String localeTag) {
        this.ruleRepository = ruleRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.deliveryRepository = deliveryRepository;
        this.pushSender = pushSender;
        this.emailSender = emailSender;
        this.iamContextFacade = iamContextFacade;
        this.maintenanceContextFacade = maintenanceContextFacade;
        this.messages = messages;
        this.clock = clock;
        this.locale = Locale.forLanguageTag(localeTag);
    }

    // ------------------------------------------------------------------ alerts (US-06, TS-08)

    public void notifyAlertRaised(AlertRaisedEvent event) {
        MaintenanceContextFacade.AssetSummary asset =
                maintenanceContextFacade.assetSummary(event.tenantId(), event.assetId()).orElse(null);
        String assetLabel = asset == null ? "?" : asset.code();

        String title = text("CRITICAL".equals(event.severity())
                ? "notification.alert.title-critical" : "notification.alert.title-warning", assetLabel);
        String body = event.metric() == null
                ? text("notification.alert.body-generic", asset == null ? assetLabel : asset.name())
                : text("notification.alert.body", text("metric." + event.metric(), event.metric()),
                plain(event.observedValue()), event.unit());
        Map<String, String> data = Map.of(
                "type", NotificationType.ALERT_RAISED.name(),
                "alertId", event.alertId().toString(),
                "assetId", event.assetId().toString());

        for (Target target : alertTargets(event.tenantId(), asset == null ? null : asset.assetType())) {
            deliver(event.tenantId(), target, NotificationType.ALERT_RAISED, event.alertId(),
                    "alert:" + event.alertId(), title, body, data);
        }
    }

    /**
     * Who receives an alert: the people with a rule for that kind of asset, each through the channel they chose.
     * If nobody configured anything, the maintenance managers by push (US-06, scenario 2).
     */
    private Set<Target> alertTargets(UUID tenantId, String assetType) {
        Set<Target> targets = new LinkedHashSet<>();
        List<NotificationRule> rules = ruleRepository.findMatching(tenantId, assetType);
        if (rules.isEmpty()) {
            for (UserContact manager : iamContextFacade.findActiveManagers(tenantId)) {
                targets.add(new Target(manager, NotificationChannel.PUSH));
            }
            return targets;
        }
        for (NotificationRule rule : rules) {
            // A person who was deactivated after the rule was created is simply left out.
            iamContextFacade.findActiveUser(tenantId, rule.getUserId())
                    .ifPresent(user -> targets.add(new Target(user, rule.getChannel())));
        }
        return targets;
    }

    // ------------------------------------------------------------------ subscription expiry (US-19)

    public void notifySubscriptionExpiring(UUID tenantId, UUID subscriptionId, Instant endsAt, long daysLeft) {
        String title = text("notification.expiry.title");
        String body = text("notification.expiry.body", daysLeft);
        Map<String, String> data = Map.of(
                "type", NotificationType.SUBSCRIPTION_EXPIRING.name(),
                "subscriptionId", subscriptionId.toString());
        // The key carries the end date: after a renewal the next expiry is a different event and notifies again.
        String dedupeKey = "expiry:" + subscriptionId + ":" + endsAt.getEpochSecond();

        for (UserContact manager : iamContextFacade.findActiveManagers(tenantId)) {
            for (NotificationChannel channel : NotificationChannel.values()) {
                deliver(tenantId, new Target(manager, channel), NotificationType.SUBSCRIPTION_EXPIRING,
                        subscriptionId, dedupeKey, title, body, data);
            }
        }
    }

    // ------------------------------------------------------------------ delivery

    private void deliver(UUID tenantId, Target target, NotificationType type, UUID subjectId, String dedupeKey,
                         String title, String body, Map<String, String> data) {
        UUID userId = target.user().id();
        try {
            if (deliveryRepository.exists(tenantId, userId, target.channel(), dedupeKey)) {
                return; // this person already received this event through this channel
            }
            Outcome outcome = target.channel() == NotificationChannel.PUSH
                    ? sendPush(tenantId, target.user(), title, body, data)
                    : sendEmail(target.user(), title, body);
            record(tenantId, userId, target.channel(), type, subjectId, dedupeKey, title, body, outcome);
        } catch (RuntimeException ex) {
            // Whatever happens here must never reach the code that created the alert.
            log.error("Notification {} to user {} through {} failed", dedupeKey, userId, target.channel(), ex);
            try {
                record(tenantId, userId, target.channel(), type, subjectId, dedupeKey, title, body,
                        new Outcome(DeliveryStatus.FAILED, "Unexpected error: " + ex.getClass().getSimpleName()));
            } catch (RuntimeException secondary) {
                log.error("The failure of notification {} could not be recorded", dedupeKey, secondary);
            }
        }
    }

    private Outcome sendPush(UUID tenantId, UserContact user, String title, String body, Map<String, String> data) {
        List<DeviceToken> devices = deviceTokenRepository.findByTenantIdAndUserId(tenantId, user.id());
        if (devices.isEmpty()) {
            return new Outcome(DeliveryStatus.SKIPPED, "The user has no registered device");
        }
        int sent = 0;
        int notConfigured = 0;
        String failure = null;
        for (DeviceToken device : devices) {
            PushResult result = pushSender.send(device.getToken(), new PushMessage(title, body, data));
            switch (result.status()) {
                case SENT -> sent++;
                case NOT_CONFIGURED -> notConfigured++;
                case INVALID_TOKEN -> {
                    deviceTokenRepository.delete(device.getId()); // an uninstalled app: stop trying
                    failure = result.detail();
                }
                case FAILED -> failure = result.detail();
            }
        }
        if (sent > 0) {
            return new Outcome(DeliveryStatus.SENT, null);
        }
        if (notConfigured > 0) {
            return new Outcome(DeliveryStatus.SKIPPED, "Push notifications are not configured on the server");
        }
        return new Outcome(DeliveryStatus.FAILED, failure);
    }

    private Outcome sendEmail(UserContact user, String title, String body) {
        if (!emailSender.isConfigured()) {
            return new Outcome(DeliveryStatus.SKIPPED, "Email is not configured on the server");
        }
        try {
            emailSender.send(user.email(), title, body + "\n\n-- PredictiveMaintain");
            return new Outcome(DeliveryStatus.SENT, null);
        } catch (EmailDeliveryException ex) {
            return new Outcome(DeliveryStatus.FAILED, ex.getMessage());
        }
    }

    private void record(UUID tenantId, UUID userId, NotificationChannel channel, NotificationType type,
                        UUID subjectId, String dedupeKey, String title, String body, Outcome outcome) {
        deliveryRepository.save(NotificationDelivery.create(tenantId, userId, channel, type, subjectId, dedupeKey,
                title, body, outcome.status(), outcome.detail(), clock.instant()));
    }

    private String text(String key, Object... args) {
        return messages.getMessage(key, args, key, locale);
    }

    private static String plain(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private record Target(UserContact user, NotificationChannel channel) {
    }

    private record Outcome(DeliveryStatus status, String detail) {
    }
}