package eu.oberonoss.tsc.cm.io.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class BinaryFileScannerTest {
    @Test
    void basicScannerTest() {
        Path startingDirectory = Path.of("../20-tsc-cm-io/src/test/resources");
        if (!Files.exists(startingDirectory)) {
            // Fallback when running from the root project working directory
            startingDirectory = Path.of("20-tsc-cm-io/src/test/resources");
        }

        assertTrue(Files.exists(startingDirectory), "Resource directory not found");

        List<File> detectedFiles = new ArrayList<>();
        BinaryFileScanner scanner = new BinaryFileScanner(startingDirectory);
        scanner.scan(file -> {
            LOGGER.info("File: {}", file);
            detectedFiles.add(file);
        });

        assertEquals(1, detectedFiles.size());
        assertEquals("01-valid.bin", detectedFiles.getFirst().getName());
    }

    @Test
    void testScanWithNestedDirectoriesAndVariousFiles(@org.junit.jupiter.api.io.TempDir Path tempDir) throws java.io.IOException {
        byte[] validHeader = new byte[]{0x53, 0x45, 0x52, 0x5A, 0x00, 0x00, 0x01, 0x00, 0x12, 0x34};
        byte[] invalidData = new byte[]{0x11, 0x22, 0x33, 0x44};

        Path rootSerz = tempDir.resolve("root.bin");
        Files.write(rootSerz, validHeader);

        Path rootInvalid = tempDir.resolve("root.txt");
        Files.write(rootInvalid, invalidData);

        Path subDir = Files.createDirectory(tempDir.resolve("sub"));
        Path subSerz = subDir.resolve("nested.bin");
        Files.write(subSerz, validHeader);

        Path subInvalid = subDir.resolve("nested.bin.txt");
        Files.write(subInvalid, invalidData);

        List<File> detectedFiles = new ArrayList<>();
        BinaryFileScanner scanner = new BinaryFileScanner(tempDir);
        scanner.scan(detectedFiles::add);

        assertEquals(2, detectedFiles.size());
        assertTrue(detectedFiles.stream().anyMatch(f -> f.getName().equals("root.bin")));
        assertTrue(detectedFiles.stream().anyMatch(f -> f.getName().equals("nested.bin")));
    }
}