package pe.edu.upc.predictivemaintain.maintenance.infrastructure.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.maintenance.application.outboundservices.EvidenceStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Keeps evidence files on the server's disk, in folders per company and work order.
 * The file name is generated here (a random UUID), never taken from the user, so a malicious
 * name like "../../etc/passwd" can never reach the disk.
 */
@Component
public class LocalEvidenceStorage implements EvidenceStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalEvidenceStorage.class);

    private final Path baseDirectory;

    public LocalEvidenceStorage(@Value("${app.evidence.storage-dir:./data/evidence}") String storageDirectory) {
        this.baseDirectory = Paths.get(storageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String store(UUID tenantId, UUID workOrderId, String extension, byte[] content) {
        String storageKey = tenantId + "/" + workOrderId + "/" + UUID.randomUUID() + "." + extension;
        Path target = resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return storageKey;
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not store the evidence file", ex);
        }
    }

    @Override
    public byte[] load(String storageKey) {
        try {
            return Files.readAllBytes(resolve(storageKey));
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read the evidence file", ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException ex) {
            log.warn("Could not delete evidence file {}: {}", storageKey, ex.getMessage());
        }
    }

    /** Resolves the key inside the base directory and refuses anything that escapes from it. */
    private Path resolve(String storageKey) {
        Path path = baseDirectory.resolve(storageKey).normalize();
        if (!path.startsWith(baseDirectory)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return path;
    }
}