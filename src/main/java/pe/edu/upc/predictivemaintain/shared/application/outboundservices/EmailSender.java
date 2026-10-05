package pe.edu.upc.predictivemaintain.shared.application.outboundservices;

/**
 * Port to send emails. Several contexts need it (password recovery, alerts, subscription notices), so it
 * lives in the shared kernel.
 */
public interface EmailSender {

    /** True when a real mail server is configured. When false, {@link #send} only writes a line in the log. */
    boolean isConfigured();

    /**
     * Sends a plain-text email.
     *
     * @throws EmailDeliveryException when the server refuses the message or does not answer
     */
    void send(String to, String subject, String body);
}