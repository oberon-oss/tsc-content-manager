package eu.oberonoss.tsc.cm.shared.mapper;

import eu.oberonoss.tsc.cm.db.entity.TscScannedFileEntity;
import lombok.Builder;

import java.time.Instant;

/**
 * Value record holding metadata from the scanned file entity, excluding the path information which forms the map key.
 *
 * @param id           The database record ID.
 * @param fileSize     The size of the file in bytes.
 * @param fileType     The type or category of the file.
 * @param lastModified The last modified timestamp of the file.
 * @param scannedAt    The timestamp when the file was scanned.
 *
 * @since 1.0.0
 */
@SuppressWarnings("JavadocDeclaration")
@Builder(toBuilder = true)
public record ScannedFileInfo(
        Long id,
        Long fileSize,
        String fileType,
        Instant lastModified,
        Instant scannedAt
) {
    /**
     * Creates a {@link ScannedFileInfo} from a {@link TscScannedFileEntity}.
     *
     * @param entity The scanned file entity.
     *
     * @return A new {@link ScannedFileInfo} instance, or {@code null} if entity is null.
     *
     * @since 1.0.0
     */
    public static ScannedFileInfo fromEntity(TscScannedFileEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ScannedFileInfo(
                entity.getId(),
                entity.getFileSize(),
                entity.getFileType(),
                entity.getLastModified(),
                entity.getScannedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public Instant getScannedAt() {
        return scannedAt;
    }
}
