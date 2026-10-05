package pe.edu.upc.predictivemaintain.notifications.application.internal.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.notifications.application.internal.services.NotificationDispatcher;
import pe.edu.upc.predictivemaintain.subscription.interfaces.acl.SubscriptionContextFacade;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Once a day it warns the managers of the subscriptions that expire within 7 days (US-19, scenario 1).
 * Running it more often (or on two servers) is harmless: the dedupe key stops repeated notices.
 */
@Component
public class SubscriptionExpiryNoticeJob {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryNoticeJob.class);
    private static final long WARNING_DAYS = 7;

    private final SubscriptionContextFacade subscriptionContextFacade;
    private final NotificationDispatcher dispatcher;
    private final Clock clock;

    public SubscriptionExpiryNoticeJob(SubscriptionContextFacade subscriptionContextFacade,
                                       NotificationDispatcher dispatcher, Clock clock) {
        this.subscriptionContextFacade = subscriptionContextFacade;
        this.dispatcher = dispatcher;
        this.clock = clock;
    }

    /** The default 14:00 UTC is 09:00 in Lima. NOTIFICATIONS_EXPIRY_CRON changes it (used to test it quickly). */
    @Scheduled(cron = "${app.notifications.expiry-cron:0 0 14 * * *}", zone = "UTC")
    public void run() {
        Instant now = clock.instant();
        for (SubscriptionContextFacade.ExpiringSubscription expiring :
                subscriptionContextFacade.findExpiringBetween(now, now.plus(WARNING_DAYS, ChronoUnit.DAYS))) {
            try {
                long secondsLeft = Duration.between(now, expiring.endsAt()).getSeconds();
                long daysLeft = (secondsLeft + 86_399) / 86_400;   // 23 hours left count as 1 day
                dispatcher.notifySubscriptionExpiring(expiring.tenantId(), expiring.subscriptionId(),
                        expiring.endsAt(), daysLeft);
            } catch (RuntimeException ex) {
                log.error("Could not notify the expiry of subscription {}", expiring.subscriptionId(), ex);
            }
        }
    }
}