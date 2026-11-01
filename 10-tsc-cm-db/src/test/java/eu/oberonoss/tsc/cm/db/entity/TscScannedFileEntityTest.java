package eu.oberonoss.tsc.cm.db.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TscScannedFileEntityTest {

    @Test
    void testEntityPropertiesAndBuilder() {
        Instant now = Instant.now();
        TscScannedFileEntity entity = TscScannedFileEntity.builder()
                .id(1L)
                .filePath("Assets/Provider/Product/Scenario.bin")
                .fileName("Scenario.bin")
                .fileSize(10240L)
                .fileType("BIN")
                .lastModified(now)
                .scannedAt(now)
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getFilePath()).isEqualTo("Assets/Provider/Product/Scenario.bin");
        assertThat(entity.getFileName()).isEqualTo("Scenario.bin");
        assertThat(entity.getFileSize()).isEqualTo(10240L);
        assertThat(entity.getFileType()).isEqualTo("BIN");
        assertThat(entity.getLastModified()).isEqualTo(now);
        assertThat(entity.getScannedAt()).isEqualTo(now);

        entity.setFileName("Updated.bin");
        assertThat(entity.getFileName()).isEqualTo("Updated.bin");

        TscScannedFileEntity copy = entity.toBuilder().fileSize(20480L).build();
        assertThat(copy.getFileSize()).isEqualTo(20480L);
    }

    @Test
    void testEqualsAndHashCode() {
        TscScannedFileEntity entity1 = TscScannedFileEntity.builder().id(100L).filePath("path1").build();
        TscScannedFileEntity entity2 = TscScannedFileEntity.builder().id(100L).filePath("path2").build();
        TscScannedFileEntity entity3 = TscScannedFileEntity.builder().id(200L).filePath("path1").build();

        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).hasSameHashCodeAs(entity2);
        assertThat(entity1).isNotEqualTo(entity3);
        assertThat(entity1.toString()).contains("100");
    }

    @Test
    void testNoArgsConstructor() {
        TscScannedFileEntity entity = new TscScannedFileEntity();
        entity.setFilePath("test/path");
        assertThat(entity.getFilePath()).isEqualTo("test/path");
    }
}
