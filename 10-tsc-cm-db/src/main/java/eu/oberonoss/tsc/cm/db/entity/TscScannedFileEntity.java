package eu.oberonoss.tsc.cm.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing a scanned file in the database.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@SuppressWarnings({"JavadocDeclaration", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "tsc_scanned_file", indexes = {
        @Index(name = "idx_tsc_scanned_file_path", columnList = "file_path")
})
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class TscScannedFileEntity {

    // @formatter:off
    /**
     * The unique identifier of the scanned file.
     * --- SETTER ---
     * Sets the unique identifier of the scanned file.
     *
     * @param id the unique identifier to set
     * --- GETTER ---
     * Returns the unique identifier of the scanned file.
     *
     * @return the unique identifier of the scanned file
     * @since 1.0.0
     */
    // @formatter:on
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // @formatter:off
    /**
     * The file path of the scanned file.
     * --- SETTER ---
     * Sets the file path of the scanned file.
     *
     * @param filePath the file path to set
     * --- GETTER ---
     * Returns the file path of the scanned file.
     *
     * @return the file path of the scanned file
     * @since 1.0.0
     */
    // @formatter:on
    @Column(name = "file_path", length = 1024, nullable = false)
    private String filePath;

    // @formatter:off
    /**
     * The name of the scanned file.
     * --- SETTER ---
     * Sets the name of the scanned file.
     *
     * @param fileName the name of the scanned file to set
     * --- GETTER ---
     * Returns the name of the scanned file.
     *
     * @return the name of the scanned file
     * @since 1.0.0
     */
    // @formatter:on
    @Column(name = "file_name", nullable = false)
    private String fileName;

    // @formatter:off
    /**
     * The size of the scanned file in bytes.
     * --- SETTER ---
     * Sets the size of the scanned file in bytes.
     *
     * @param fileSize the size of the scanned file in bytes to set
     * --- GETTER ---
     * Returns the size of the scanned file in bytes.
     *
     * @return the size of the scanned file in bytes
     * @since 1.0.0
     */
    // @formatter:on
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    // @formatter:off
    /**
     * The type of the scanned file.
     * --- SETTER ---
     * Sets the type of the scanned file.
     *
     * @param fileType the type of the scanned file to set
     * --- GETTER ---
     * Returns the type of the scanned file.
     *
     * @return the type of the scanned file
     * @since 1.0.0
     */
    // @formatter:on
    @Column(name = "file_type", length = 50)
    private String fileType;

    // @formatter:off
    /**
     * The timestamp when the file was last modified.
     * --- SETTER ---
     * Sets the timestamp when the file was last modified.
     *
     * @param lastModified the timestamp when the file was last modified to set
     * --- GETTER ---
     * Returns the timestamp when the file was last modified.
     *
     * @return the timestamp when the file was last modified
     * @since 1.0.0
     */
    // @formatter:on
    @Column(name = "last_modified")
    private Instant lastModified;

    // @formatter:off
    /**
     * The timestamp when the file was scanned.
     * --- SETTER ---
     * Sets the timestamp when the file was scanned.
     *
     * @param scannedAt the timestamp when the file was scanned to set
     * --- GETTER ---
     * Returns the timestamp when the file was scanned.
     *
     * @return the timestamp when the file was scanned
     * @since 1.0.0
     */
    // @formatter:on
    @Column(name = "scanned_at")
    private Instant scannedAt;
}
