package pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates;

import java.time.Instant;
import java.util.UUID;

/**
 * A photo attached to a work order. The file itself lives in the evidence storage; here we only keep
 * its opaque key and its type.
 */
public record EvidencePhoto(UUID id, UUID tenantId, UUID workOrderId, String storageKey,
                            String mimeType, Instant uploadedAt) {

    public static EvidencePhoto create(UUID tenantId, UUID workOrderId, String storageKey,
                                       String mimeType, Instant uploadedAt) {
        return new EvidencePhoto(UUID.randomUUID(), tenantId, workOrderId, storageKey, mimeType, uploadedAt);
    }
}