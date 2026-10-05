package pe.edu.upc.predictivemaintain.notifications.application.internal.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.events.AlertRaisedEvent;
import pe.edu.upc.predictivemaintain.notifications.application.internal.services.NotificationDispatcher;

/**
 * Reacts to a new alert. Two annotations make it safe:
 * AFTER_COMMIT: it runs only once the alert is really saved, so nobody is notified about an alert that was rolled back.
 * @Async: it runs in another thread, so the sensor that triggered the alert does not wait for the push service.
 */
@Component
public class AlertRaisedNotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(AlertRaisedNotificationHandler.class);

    private final NotificationDispatcher dispatcher;

    public AlertRaisedNotificationHandler(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(AlertRaisedEvent event) {
        try {
            dispatcher.notifyAlertRaised(event);
        } catch (RuntimeException ex) {
            log.error("Could not notify alert {}", event.alertId(), ex);
        }
    }
}