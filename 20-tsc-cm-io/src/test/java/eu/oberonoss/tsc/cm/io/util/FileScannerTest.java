package eu.oberonoss.tsc.cm.io.util;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class FileScannerTest {
    @Test
    void basicScannerTest() {
        Path startingDirectory = Path.of("../20-tsc-cm-io/src/test/resources");
        if (!Files.exists(startingDirectory)) {
            // Fallback when running from the root project working directory
            startingDirectory = Path.of("20-tsc-cm-io/src/test/resources");
        }

        assertTrue(Files.exists(startingDirectory), "Resource directory not found");

        List<BinaryDataReader> detectedReaders = new CopyOnWriteArrayList<>();
        FileScanner scanner = FileScanner.of();
        scanner.scan(new BinaryFileProcessor(reader -> {
            LOGGER.info("Read bytes: {}", reader.getViewer().size());
            detectedReaders.add(reader);
        }), startingDirectory);

        assertEquals(1, detectedReaders.size());
        assertTrue(detectedReaders.getFirst().getViewer().size() > 0);
        assertEquals((byte) 0x53, detectedReaders.getFirst().getViewer().peekByte(0));
    }

    @Test
    void testScanWithNestedDirectoriesAndVariousFiles(@org.junit.jupiter.api.io.TempDir Path tempDir) throws java.io.IOException {
        byte[] validHeader = {0x53, 0x45, 0x52, 0x5A, 0x00, 0x00, 0x01, 0x00, 0x12, 0x34};
        byte[] invalidData = {0x11, 0x22, 0x33, 0x44};

        Path rootSerz = tempDir.resolve("root.bin");
        Files.write(rootSerz, validHeader);

        Path rootInvalid = tempDir.resolve("root.txt");
        Files.write(rootInvalid, invalidData);

        Path subDir = Files.createDirectory(tempDir.resolve("sub"));
        Path subSerz = subDir.resolve("nested.bin");
        Files.write(subSerz, validHeader);

        Path subInvalid = subDir.resolve("nested.bin.txt");
        Files.write(subInvalid, invalidData);

        List<BinaryDataReader> detectedReaders = new CopyOnWriteArrayList<>();
        FileScanner scanner = FileScanner.of();
        scanner.scan(new BinaryFileProcessor(detectedReaders::add), tempDir);

        assertEquals(2, detectedReaders.size());
        assertEquals(10, detectedReaders.get(0).getViewer().size());
        assertEquals(10, detectedReaders.get(1).getViewer().size());
    }
    @Test
    void testFileScannerImplDirectInstantiation() {
        FileScannerImpl defaultScanner = new FileScannerImpl();
        assertEquals(FileScanner.DEFAULT_THREAD_COUNT, defaultScanner.threadCount());

        FileScannerImpl customScanner = new FileScannerImpl(8);
        assertEquals(8, customScanner.threadCount());

        FileScannerImpl zeroScanner = new FileScannerImpl(0);
        assertEquals(1, zeroScanner.threadCount());

        FileScanner ofScanner = FileScanner.of(6);
        assertEquals(6, ofScanner.threadCount());
    }

    @Test
    void testScanNonExistentPath() {
        FileScanner scanner = FileScanner.of();
        List<BinaryDataReader> detected = new CopyOnWriteArrayList<>();
        scanner.scan(new BinaryFileProcessor(detected::add), Path.of("non-existent-path-for-testing-12345"));
        assertTrue(detected.isEmpty());
    }
}