package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

import java.util.Optional;

/**
 * Image formats accepted as evidence. The format is detected from the first bytes of the file
 * (its "magic number"), not from the file name or the Content-Type header, because both can be faked.
 */
public enum ImageFormat {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String mimeType;
    private final String extension;

    ImageFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String mimeType() {
        return mimeType;
    }

    public String extension() {
        return extension;
    }

    public static Optional<ImageFormat> detect(byte[] content) {
        if (content == null || content.length < 12) {
            return Optional.empty();
        }
        if (startsWith(content, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of(JPEG);
        }
        if (startsWith(content, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of(PNG);
        }
        // WebP: "RIFF" at the start and "WEBP" at byte 8.
        if (startsWith(content, 0, 0x52, 0x49, 0x46, 0x46) && startsWith(content, 8, 0x57, 0x45, 0x42, 0x50)) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] content, int offset, int... expected) {
        for (int i = 0; i < expected.length; i++) {
            if ((content[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }
}