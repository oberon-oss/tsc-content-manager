package eu.oberonoss.tsc.cm.base;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataReaderImpl;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides methods for loading binary data from input streams and file paths.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface ByteLoader {
    /**
     * Loads binary data from an input stream.
     *
     * @param inputStream The input stream to read binary data from.
     *
     * @return The binary data read from the input stream.
     *
     * @throws IOException If an I/O error occurs while reading the input stream.
     * @since 1.0.0
     */
    default byte[] loadBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        byte[] buffer = new byte[16 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            target.write(buffer, 0, bytesRead);
        }
        return target.toByteArray();
    }

    /**
     * Loads binary data from a file path.
     *
     * @param path The file path to read binary data from.
     *
     * @return The binary data read from the file.
     *
     * @throws IOException If an I/O error occurs while reading the file.
     */
    default byte[] loadBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    /**
     * Creates and returns a BinaryDataViewer to view the given binary data.
     *
     * @param path The file path to read binary data from.
     *
     * @return A BinaryDataViewer instance for the provided binary data.
     *
     * @throws IOException If an I/O error occurs while reading the file.
     * @since 1.0.0
     */
    default BinaryDataViewer getBinaryDataViewer(Path path) throws IOException {
        return new BinaryDataViewerImpl(loadBytes(path));
    }

    /**
     * Creates and returns a BinaryDataReader to read the given binary data.
     *
     * @param path The file path to read binary data from.
     *
     * @return A BinaryDataReader instance for the provided binary data.
     *
     * @throws IOException If an I/O error occurs while reading the file.
     * @since 1.0.0
     */
    default BinaryDataReader getBinaryDataReader(Path path) throws IOException {
        return new BinaryDataReaderImpl(getBinaryDataViewer(path));
    }

    /**
     * Creates and returns a BinaryDataViewer to view the given binary data.
     *
     * @param inputStream The input stream to read binary data from.
     *
     * @return A BinaryDataViewer instance for the provided binary data.
     *
     * @throws IOException If an I/O error occurs while reading the input stream.
     * @since 1.0.0
     */
    default BinaryDataViewer getBinaryDataViewer(InputStream inputStream) throws IOException {
        return new BinaryDataViewerImpl(loadBytes(inputStream));
    }

    /**
     * Creates and returns a BinaryDataReader to read the given binary data.
     *
     * @param inputStream The input stream to read binary data from.
     *
     * @return A BinaryDataReader instance for the provided binary data.
     *
     * @throws IOException If an I/O error occurs while reading the input stream.
     * @since 1.0.0
     */
    default BinaryDataReader getBinaryDataReader(InputStream inputStream) throws IOException {
        return new BinaryDataReaderImpl(getBinaryDataViewer(inputStream));
    }
}
