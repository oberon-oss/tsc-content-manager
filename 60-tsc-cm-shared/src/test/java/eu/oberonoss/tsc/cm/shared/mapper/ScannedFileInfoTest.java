package eu.oberonoss.tsc.cm.shared.mapper;

import eu.oberonoss.tsc.cm.db.entity.TscScannedFileEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ScannedFileInfoTest {

    @Test
    void testRecordCreationAndGetters() {
        Instant now = Instant.now();
        Instant scanned = now.plusSeconds(10);

        ScannedFileInfo info = new ScannedFileInfo(101L, 2048L, "bin", now, scanned);

        assertEquals(101L, info.id());
        assertEquals(101L, info.getId());
        assertEquals(2048L, info.fileSize());
        assertEquals(2048L, info.getFileSize());
        assertEquals("bin", info.fileType());
        assertEquals("bin", info.getFileType());
        assertEquals(now, info.lastModified());
        assertEquals(now, info.getLastModified());
        assertEquals(scanned, info.scannedAt());
        assertEquals(scanned, info.getScannedAt());
    }

    @Test
    void testFromEntity() {
        assertNull(ScannedFileInfo.fromEntity(null));

        Instant now = Instant.now();
        Instant scanned = now.plusSeconds(5);
        TscScannedFileEntity entity = TscScannedFileEntity.builder()
                .id(42L)
                .filePath("Assets/Kuju/RailSimulatorCore")
                .fileName("test.bin")
                .fileSize(1024L)
                .fileType("bin")
                .lastModified(now)
                .scannedAt(scanned)
                .build();

        ScannedFileInfo info = ScannedFileInfo.fromEntity(entity);
        assertNotNull(info);
        assertEquals(42L, info.getId());
        assertEquals(1024L, info.getFileSize());
        assertEquals("bin", info.getFileType());
        assertEquals(now, info.getLastModified());
        assertEquals(scanned, info.getScannedAt());
    }

    @Test
    void testBuilderAndToBuilder() {
        Instant now = Instant.now();
        ScannedFileInfo info = ScannedFileInfo.builder()
                .id(1L)
                .fileSize(500L)
                .fileType("xml")
                .lastModified(now)
                .scannedAt(now)
                .build();

        assertEquals(1L, info.getId());
        assertEquals(500L, info.getFileSize());
        assertEquals("xml", info.getFileType());

        ScannedFileInfo updated = info.toBuilder()
                .fileSize(1000L)
                .fileType("json")
                .build();

        assertEquals(1L, updated.getId());
        assertEquals(1000L, updated.getFileSize());
        assertEquals("json", updated.getFileType());
        assertEquals(now, updated.getLastModified());
    }

    @Test
    void testEqualsAndHashCodeAndToString() {
        Instant now = Instant.now();
        ScannedFileInfo info1 = new ScannedFileInfo(1L, 100L, "bin", now, now);
        ScannedFileInfo info2 = new ScannedFileInfo(1L, 100L, "bin", now, now);
        ScannedFileInfo info3 = new ScannedFileInfo(2L, 200L, "xml", now, now);

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
        assertNotEquals(info1, info3);
        assertNotNull(info1.toString());
        assertTrue(info1.toString().contains("ScannedFileInfo"));
    }
}
