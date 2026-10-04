package pe.edu.upc.predictivemaintain.maintenance.application.outboundservices;

import java.util.UUID;

/**
 * Port to the place where evidence files are kept (local disk now, cloud storage in the future).
 */
public interface EvidenceStorage {

    /** Stores the file and returns an opaque key to find it later. */
    String store(UUID tenantId, UUID workOrderId, String extension, byte[] content);

    byte[] load(String storageKey);

    /** Best effort: never fails. */
    void delete(String storageKey);
}