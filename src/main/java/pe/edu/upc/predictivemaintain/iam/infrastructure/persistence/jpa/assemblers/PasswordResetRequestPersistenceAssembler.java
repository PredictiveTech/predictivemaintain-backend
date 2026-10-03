package pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.PasswordResetRequest;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.entities.PasswordResetRequestPersistenceEntity;

public final class PasswordResetRequestPersistenceAssembler {

    private PasswordResetRequestPersistenceAssembler() {
    }

    public static PasswordResetRequest toDomain(PasswordResetRequestPersistenceEntity entity) {
        return PasswordResetRequest.restore(entity.getId(), entity.getTenantId(), entity.getUserId(),
                entity.getTokenHash(), entity.getExpiresAt(), entity.getUsedAt());
    }

    public static void copyToEntity(PasswordResetRequest request, PasswordResetRequestPersistenceEntity entity) {
        entity.setId(request.getId());
        entity.setTenantId(request.getTenantId());
        entity.setUserId(request.getUserId());
        entity.setTokenHash(request.getTokenHash());
        entity.setExpiresAt(request.getExpiresAt());
        entity.setUsedAt(request.getUsedAt());
    }
}