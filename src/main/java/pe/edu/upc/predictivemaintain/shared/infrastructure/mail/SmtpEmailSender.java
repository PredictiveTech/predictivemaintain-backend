package pe.edu.upc.predictivemaintain.shared.infrastructure.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.shared.application.outboundservices.EmailDeliveryException;
import pe.edu.upc.predictivemaintain.shared.application.outboundservices.EmailSender;

import java.util.Properties;

/**
 * Sends email through an SMTP server. It exists only when app.mail.host is set (MAIL_HOST); otherwise
 * {@link LoggingEmailSender} is used. The timeouts matter: without them a server that does not answer
 * would keep a thread waiting forever.
 */
@Component
@ConditionalOnExpression("'${app.mail.host:}' != ''")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    private final String from;

    public SmtpEmailSender(@Value("${app.mail.host}") String host,
                           @Value("${app.mail.port:587}") int port,
                           @Value("${app.mail.username:}") String username,
                           @Value("${app.mail.password:}") String password,
                           @Value("${app.mail.from}") String from) {
        this.from = from;
        mailSender.setHost(host);
        mailSender.setPort(port);
        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        properties.put("mail.smtp.writetimeout", "5000");
        if (!username.isBlank()) {
            mailSender.setUsername(username);
            mailSender.setPassword(password);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
        }
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new EmailDeliveryException("The mail server could not deliver the message", ex);
        }
    }
}