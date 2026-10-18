package eu.oberonoss.tsc.cm.io.util;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.util.Arrays;

/**
 * Examines given files and determines if they are binary or XML serz files.
 * <p>
 * A binary SERZ file is a file with the following signature as specified in the {@link #SERZ_ID_SIGNATURE}.
 * <p>
 * An XML SERZ file is an XML file where the XML header is directly followed by either {@code <cBlueprintLoader>} or {@code <cDeltaLoader>}.
 *
 * @author TigerLilly64
 * @since v1.0.0
 */
@SuppressWarnings("HttpUrlsUsage")
public class SerzFileTypeDetector extends FileTypeDetector {
    /// Identifies the SERZ binary file format.
    public static final String SERZ_BINARY_CONTENT_TYPE = "application/x-serz";

    /// Identifies the SERZ XML file format.
    public static final String SERZ_XML_CONTENT_TYPE = "application/x-serz+xml";

    /// The first line of an XML SERZ file.
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    /// Line stating the structure name as `<cBlueprintLoader>`.
    public static final String BLUEPRINT_LOADER_HEADER = "<cBlueprintLoader xmlns:d=\"http://www.kuju.com/TnT/2003/Delta\" d:version=\"1.0\">";

    /// Line stating the structure name as `<cDeltaLoader>`.
    public static final String DELTA_LOADER_HEADER = "<cDeltaLoader xmlns:d=\"http://www.kuju.com/TnT/2003/Delta\" d:version=\"1.0\">";

    private static final byte[] SERZ_ID_SIGNATURE = new byte[]{0x53, 0x45, 0x52, 0x5A, 0x00, 0x00, 0x01, 0x00};

    /**
     * Default constructor.
     *
     * @since 1.0.0
     */
    public SerzFileTypeDetector() {
        // Keep Javadoc happy
    }

    @Override
    public String probeContentType(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            if (isValidBinarySerzHeader(is.readNBytes(8))) {
                return SERZ_BINARY_CONTENT_TYPE;
            }
        }
        try (InputStream is = Files.newInputStream(path)) {
            if (isXmlSerzFile(is)) {
                return SERZ_XML_CONTENT_TYPE;
            }
        }
        return null;
    }

    /**
     * Examines the given header if it's content contains a valid binary serz file signature.
     *
     * @param header The header to examine. Must not be null and must have at least 8 bytes.
     *
     * @return True if the header contains a valid serz file signature, false otherwise, or if the header is null or shorter than 8 bytes
     *
     * @since v1.0.0
     */
    public static boolean isValidBinarySerzHeader(byte[] header) {
        if (header == null || header.length < 8) {
            return false;
        }

        return (Arrays.equals(SERZ_ID_SIGNATURE, 0, 8, header, 0, 8));
    }

    /**
     * Determines if the given input stream is an XML serz file.
     *
     * @param is The input stream to examine. Must not be null.
     *
     * @return True if the input stream is an XML serz file, false otherwise.
     *
     * @throws IOException If an I/O error occurs reading from the input stream.
     * @since v1.0.0
     */
    private static boolean isXmlSerzFile(@NotNull InputStream is) throws IOException {
        char[] buffer = new char[512];
        int readCount;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            readCount = reader.read(buffer, 0, buffer.length);
        }

        if (readCount < XML_HEADER.length()) {
            return false;
        }

        String header = new String(buffer, 0, readCount);
        if (header.startsWith("\uFEFF")) {
            header = header.substring(1);
        }

        header = header.stripLeading();
        if (!header.startsWith(XML_HEADER)) {
            return false;
        }

        String remainder = header.substring(XML_HEADER.length()).stripLeading();
        while (remainder.startsWith("<!--")) {
            int commentEnd = remainder.indexOf("-->");
            if (commentEnd == -1) {
                return false;
            }
            remainder = remainder.substring(commentEnd + 3).stripLeading();
        }
        return remainder.startsWith(BLUEPRINT_LOADER_HEADER) || remainder.startsWith(DELTA_LOADER_HEADER);
    }
}
