package eu.oberonoss.tsc.cm.base;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ByteLoaderTest {

    private final ByteLoader loader = ByteLoaderImpl.getInstance();

    @Test
    void testGetInstanceSingleton() {
        ByteLoader instance1 = ByteLoaderImpl.getInstance();
        ByteLoader instance2 = ByteLoaderImpl.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    void testLoadBytesFromInputStream() throws IOException {
        byte[] expected = {1, 2, 3, 4, 5, 6, 7, 8};
        try (InputStream in = new ByteArrayInputStream(expected)) {
            byte[] actual = loader.loadBytes(in);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    void testLoadBytesFromPath(@TempDir Path tempDir) throws IOException {
        byte[] expected = {10, 20, 30, 40};
        Path testFile = tempDir.resolve("test.bin");
        Files.write(testFile, expected);

        byte[] actual = loader.loadBytes(testFile);
        assertArrayEquals(expected, actual);
    }

    @Test
    void testGetBinaryDataViewerFromPath(@TempDir Path tempDir) throws IOException {
        byte[] data = {0x53, 0x45, 0x52, 0x5A};
        Path testFile = tempDir.resolve("viewer-test.bin");
        Files.write(testFile, data);

        BinaryDataViewer viewer = loader.getBinaryDataViewer(testFile);
        assertNotNull(viewer);
        assertEquals(0x53, viewer.peekByte(0));
        assertEquals(0x45, viewer.peekByte(1));
    }

    @Test
    void testGetBinaryDataReaderFromPath(@TempDir Path tempDir) throws IOException {
        byte[] data = {0x53, 0x45, 0x52, 0x5A};
        Path testFile = tempDir.resolve("reader-test.bin");
        Files.write(testFile, data);

        BinaryDataReader reader = loader.getBinaryDataReader(testFile);
        assertNotNull(reader);
        assertEquals(0, reader.offset());
        assertEquals(4, reader.remaining());
        assertNotNull(reader.getViewer());
    }

    @Test
    void testGetBinaryDataViewerFromInputStream() throws IOException {
        byte[] data = {0x11, 0x22, 0x33};
        try (InputStream in = new ByteArrayInputStream(data)) {
            BinaryDataViewer viewer = loader.getBinaryDataViewer(in);
            assertNotNull(viewer);
            assertEquals(0x11, viewer.peekByte(0));
            assertEquals(0x22, viewer.peekByte(1));
        }
    }

    @Test
    void testGetBinaryDataReaderFromInputStream() throws IOException {
        byte[] data = {0x11, 0x22, 0x33};
        try (InputStream in = new ByteArrayInputStream(data)) {
            BinaryDataReader reader = loader.getBinaryDataReader(in);
            assertNotNull(reader);
            assertEquals(0, reader.offset());
            assertEquals(3, reader.remaining());
            assertEquals(0x11, reader.getViewer().peekByte(0));
        }
    }
}
