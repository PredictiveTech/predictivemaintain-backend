package pe.edu.upc.predictivemaintain.notifications.application.outboundservices;

import java.util.Map;

/**
 * What a push notification says. The data travels with it so the app knows what to open when it is tapped.
 * Do not put anything sensitive here: the text goes through the push provider's servers.
 */
public record PushMessage(String title, String body, Map<String, String> data) {
}