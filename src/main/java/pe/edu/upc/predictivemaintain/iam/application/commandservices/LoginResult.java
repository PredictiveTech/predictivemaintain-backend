package pe.edu.upc.predictivemaintain.iam.application.commandservices;

import pe.edu.upc.predictivemaintain.iam.application.outboundservices.IssuedToken;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;

public record LoginResult(UserAccount user, IssuedToken token) {
}