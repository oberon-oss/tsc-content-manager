package eu.oberonoss.tsc.cm.io.util;

import eu.oberonoss.tsc.cm.base.SERZTagTypes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BinarySERZDataScannerTest {
    private final BinarySERZDataScanner scanner = new BinarySERZDataScanner();

    @Test
    void testScanner() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/util/serzdetector/01-valid.bin")) {
            assertNotNull(inputStream, "Resource /util/serzdetector/01-valid.bin not found on classpath");
            List<SERZTagInfo> serzTagInfos = scanner.scanBinarySERZData(inputStream);

            assertNotNull(serzTagInfos);
            assertEquals(51, serzTagInfos.size());

            // First tag: FILE_HEADER
            SERZTagInfo first = serzTagInfos.getFirst();
            assertEquals(SERZTagTypes.FILE_HEADER, first.getTagType());
            assertEquals(0, first.getTagOffset());
            assertEquals(8, first.getDataOffset());
            assertEquals(0, first.getDataLength());
            assertNotNull(first.getViewer());

            // Last tag: TYPE_X70
            SERZTagInfo last = serzTagInfos.getLast();
            assertEquals(SERZTagTypes.TYPE_X70, last.getTagType());
            assertEquals(1336, last.getTagOffset());
            assertEquals(1338, last.getDataOffset());
            assertEquals(2, last.getDataLength());
            assertNotNull(last.getViewer());

            // Verify continuous coverage
            for (int i = 0; i < serzTagInfos.size() - 1; i++) {
                SERZTagInfo current = serzTagInfos.get(i);
                SERZTagInfo next = serzTagInfos.get(i + 1);
                assertEquals(current.getDataOffset() + current.getDataLength(), next.getTagOffset(),
                        "Gap or overlap detected between tag " + i + " and " + (i + 1));
            }
        }
    }

    @Test
    void testEmptyStream() throws IOException {
        try (InputStream inputStream = new java.io.ByteArrayInputStream(new byte[0])) {
            List<SERZTagInfo> serzTagInfos = scanner.scanBinarySERZData(inputStream);
            assertNotNull(serzTagInfos);
            assertTrue(serzTagInfos.isEmpty());
        }
    }

    @Test
    void testStreamWithoutRecognizedTags() throws IOException {
        byte[] nonTagData = {1, 2, 3, 4, 5, 6, 7, 8};
        try (InputStream inputStream = new java.io.ByteArrayInputStream(nonTagData)) {
            List<SERZTagInfo> serzTagInfos = scanner.scanBinarySERZData(inputStream);
            assertNotNull(serzTagInfos);
            assertTrue(serzTagInfos.isEmpty());
        }
    }
}