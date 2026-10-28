package eu.oberonoss.tsc.cm.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.*;

class SERZTagDetectorTest {

    private static final Path TEST_DIR = Paths.get("../20-tsc-cm-io/src/test/resources/util/serzdetector");

    @Test
    void testDefaultConstructor() {
        SERZTagDetector detector = new SERZTagDetector();
        assertEquals(4, detector.getThreadCount());
        assertTrue(detector.isIncludeClosingTag());
        assertNotNull(detector.getAggregatedCounts());
        assertTrue(detector.getAggregatedCounts().isEmpty());
    }

    @Test
    void testCustomConstructors() {
        SERZTagDetector detector1 = new SERZTagDetector(8);
        assertEquals(8, detector1.getThreadCount());
        assertTrue(detector1.isIncludeClosingTag());

        SERZTagDetector detector2 = new SERZTagDetector(false);
        assertEquals(4, detector2.getThreadCount());
        assertFalse(detector2.isIncludeClosingTag());

        SERZTagDetector detector3 = new SERZTagDetector(2, false);
        assertEquals(2, detector3.getThreadCount());
        assertFalse(detector3.isIncludeClosingTag());
    }

    @Test
    void testDetectWithTargetBytes() {
        SERZTagDetector detector = new SERZTagDetector(2, true);
        Map<Byte, LongAdder> counts = detector.detect(TEST_DIR, Set.of((byte) 0x50, (byte) 0x56));

        assertNotNull(counts);
        assertSame(counts, detector.getAggregatedCounts());
        assertFalse(counts.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDetectWithClosingTagInclusion(boolean includeClosingTag) {
        SERZTagDetector detector = new SERZTagDetector(2, includeClosingTag);
        Map<Byte, LongAdder> counts = detector.detect(TEST_DIR, Set.of((byte) 0x50));

        if (includeClosingTag) {
            assertTrue(counts.containsKey(SERZTagDetector.CLOSING_TAG_BYTE));
        } else {
            assertFalse(counts.containsKey(SERZTagDetector.CLOSING_TAG_BYTE));
        }
    }

    @Test
    void testDetectWithDefaultTags() {
        SERZTagDetector detector = new SERZTagDetector();
        Map<Byte, LongAdder> counts = detector.detect(TEST_DIR);

        assertNotNull(counts);
        assertFalse(counts.isEmpty());
    }

    @Test
    void testDetectWithCustomIdStrings() {
        SERZTagDetector detector = new SERZTagDetector();
        Map<Byte, LongAdder> counts = detector.detect(TEST_DIR, List.of("0x50", "0x56"), false);

        assertNotNull(counts);
        assertFalse(counts.isEmpty());
    }

    @Test
    void testDetectWithUnknownTags() {
        SERZTagDetector detector = new SERZTagDetector();
        Map<Byte, LongAdder> counts = detector.detect(TEST_DIR, null, true);

        assertNotNull(counts);
    }

    @Test
    void testDetectWithEmptyDirectory(@TempDir Path tempDir) {
        SERZTagDetector detector = new SERZTagDetector();
        Map<Byte, LongAdder> counts = detector.detect(tempDir);

        assertNotNull(counts);
        assertTrue(counts.isEmpty());
    }

    @Test
    void testParseIdBytes() {
        Set<Byte> parsed = SERZTagDetector.parseIdBytes(List.of("0x50", "0x56", "invalid", "0x41"));
        assertEquals(3, parsed.size());
        assertTrue(parsed.contains((byte) 0x50));
        assertTrue(parsed.contains((byte) 0x56));
        assertTrue(parsed.contains((byte) 0x41));

        assertTrue(SERZTagDetector.parseIdBytes(null).isEmpty());
        assertTrue(SERZTagDetector.parseIdBytes(List.of()).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0xFF, 0x50, 0x56, 0x41, 0x42, 0x43})
    void testUnknownTagsExcludesDefaultAndSpecialIds(int id) {
        Set<Byte> idBytes = SERZTagDetector.getIDBytes(null, true);

        assertFalse(idBytes.contains((byte) id), () -> String.format("0x%02X should be excluded from unknown tags", id));
    }

    @Test
    void testUnknownTagsTotalCount() {
        Set<Byte> idBytes = SERZTagDetector.getIDBytes(null, true);

        assertEquals(256 - 5 - 1, idBytes.size());
    }

    private static final byte[] SERZ_HEADER = {0x53, 0x45, 0x52, 0x5A, 0x00, 0x00, 0x01, 0x00};

    @Test
    void testDetectWithTrailingMarkerBytes(@TempDir Path tempDir) throws java.io.IOException {
        // File with marker byte at EOF - 2 (3 bytes left: FF, 0x01, 0x02) - not a full 4-byte tag
        Path file1 = tempDir.resolve("trailing1.bin");
        byte[] content1 = new byte[SERZ_HEADER.length + 3];
        System.arraycopy(SERZ_HEADER, 0, content1, 0, SERZ_HEADER.length);
        content1[SERZ_HEADER.length] = (byte) 0xFF;
        content1[SERZ_HEADER.length + 1] = 0x50;
        content1[SERZ_HEADER.length + 2] = 0x01;
        java.nio.file.Files.write(file1, content1);

        // File with marker byte at EOF - 1 (2 bytes left: FF, 0x70) - closing tag
        Path file2 = tempDir.resolve("trailing2.bin");
        byte[] content2 = new byte[SERZ_HEADER.length + 2];
        System.arraycopy(SERZ_HEADER, 0, content2, 0, SERZ_HEADER.length);
        content2[SERZ_HEADER.length] = (byte) 0xFF;
        content2[SERZ_HEADER.length + 1] = 0x70;
        java.nio.file.Files.write(file2, content2);

        // File with marker byte at EOF (1 byte left: FF)
        Path file3 = tempDir.resolve("trailing3.bin");
        byte[] content3 = new byte[SERZ_HEADER.length + 1];
        System.arraycopy(SERZ_HEADER, 0, content3, 0, SERZ_HEADER.length);
        content3[SERZ_HEADER.length] = (byte) 0xFF;
        java.nio.file.Files.write(file3, content3);

        SERZTagDetector detector = new SERZTagDetector(2, true);
        Map<Byte, LongAdder> counts = detector.detect(tempDir, Set.of((byte) 0x50));

        assertNotNull(counts);
        // Closing tag in file2 should be detected
        assertTrue(counts.containsKey(SERZTagDetector.CLOSING_TAG_BYTE));
        assertEquals(1, counts.get(SERZTagDetector.CLOSING_TAG_BYTE).sum());
        // file1 has FF 50 01 (not FF 50 FF FF), so 0x50 should not match 4-byte tag
        assertFalse(counts.containsKey((byte) 0x50));
    }

    @Test
    void testDetectWithFullTagAtEnd(@TempDir Path tempDir) throws java.io.IOException {
        Path file = tempDir.resolve("endtag.bin");
        byte[] content = new byte[SERZ_HEADER.length + 4];
        System.arraycopy(SERZ_HEADER, 0, content, 0, SERZ_HEADER.length);
        content[SERZ_HEADER.length] = (byte) 0xFF;
        content[SERZ_HEADER.length + 1] = 0x50;
        content[SERZ_HEADER.length + 2] = (byte) 0xFF;
        content[SERZ_HEADER.length + 3] = (byte) 0xFF;
        java.nio.file.Files.write(file, content);

        SERZTagDetector detector = new SERZTagDetector();
        Map<Byte, LongAdder> counts = detector.detect(tempDir, Set.of((byte) 0x50));

        assertNotNull(counts);
        assertTrue(counts.containsKey((byte) 0x50));
        assertEquals(1, counts.get((byte) 0x50).sum());
    }

    @Test
    void testGetIDBytesWithCustomList() {
        Set<Byte> idBytes = SERZTagDetector.getIDBytes(List.of("0x12", "0x34"), false);
        assertEquals(2, idBytes.size());
        assertTrue(idBytes.contains((byte) 0x12));
        assertTrue(idBytes.contains((byte) 0x34));
    }
}
