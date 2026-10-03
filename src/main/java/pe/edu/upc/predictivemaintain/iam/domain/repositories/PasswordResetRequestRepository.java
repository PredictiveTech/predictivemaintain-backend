package pe.edu.upc.predictivemaintain.iam.domain.repositories;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.PasswordResetRequest;

import java.util.Optional;

public interface PasswordResetRequestRepository {

    PasswordResetRequest save(PasswordResetRequest request);

    Optional<PasswordResetRequest> findByTokenHash(String tokenHash);
}