package pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.DisplayName;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.entities.UserAccountPersistenceEntity;

public final class UserAccountPersistenceAssembler {

    private UserAccountPersistenceAssembler() {
    }

    public static UserAccount toDomain(UserAccountPersistenceEntity entity) {
        return UserAccount.restore(
                entity.getId(),
                entity.getTenantId(),
                new EmailAddress(entity.getEmail()),
                new DisplayName(entity.getDisplayName()),
                entity.getPasswordHash(),
                entity.isActive(),
                entity.getRoles());
    }

    public static void copyToEntity(UserAccount user, UserAccountPersistenceEntity entity) {
        entity.setId(user.getId());
        entity.setTenantId(user.getTenantId());
        entity.setEmail(user.getEmail().value());
        entity.setDisplayName(user.getDisplayName().value());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setActive(user.isActive());
        // Modify the managed collection instead of replacing it so Hibernate detects the changes.
        entity.getRoles().clear();
        entity.getRoles().addAll(user.getRoles());
    }
}