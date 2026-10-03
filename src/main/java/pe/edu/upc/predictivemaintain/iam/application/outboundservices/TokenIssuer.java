package pe.edu.upc.predictivemaintain.iam.application.outboundservices;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;

/**
 * Port to issue access tokens for an authenticated user.
 */
public interface TokenIssuer {

    IssuedToken issue(UserAccount user);
}