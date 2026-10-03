package pe.edu.upc.predictivemaintain.iam.application.outboundservices;

import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;

/**
 * Port to deliver the password recovery link to the user.
 */
public interface PasswordResetNotifier {

    void sendResetLink(EmailAddress email, String token);
}