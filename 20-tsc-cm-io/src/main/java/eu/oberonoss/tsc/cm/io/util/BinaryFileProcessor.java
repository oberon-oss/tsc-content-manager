package eu.oberonoss.tsc.cm.io.util;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberonoss.tsc.cm.base.FileDataType;

import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

/**
 * A processor for handling binary SERZ files.
 * <p>
 * The processor uses a predefined {@link FileFilter} to filter files based on their binary SERZ signature.
 *
 * @param consumer Instance of the {@link Consumer} interface, allowing the handling of the loaded binary data reader as needed by the client.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public record BinaryFileProcessor(Consumer<BinaryDataReader> consumer) implements FileProcessor<BinaryDataReader> {
    private static final FileFilter fileFilter = file -> {
        if (file.isDirectory()) {
            return true;
        }
        try {
            return SerzFileTypeDetector.SERZ_BINARY_CONTENT_TYPE.equalsIgnoreCase(Files.probeContentType(file.toPath()));
        } catch (IOException _) {
            return false;
        }
    };

    private static final FileDataType fileDataType = FileDataType.SERZ_BINARY;

    @Override
    public FileDataType getFileDataType() {
        return fileDataType;
    }

    @Override
    public FileFilter getFileFilter() {
        return fileFilter;
    }
}
