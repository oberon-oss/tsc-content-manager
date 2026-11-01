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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "file_path", length = 1024, nullable = false)
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "last_modified")
    private Instant lastModified;

    @Column(name = "scanned_at")
    private Instant scannedAt;
}
