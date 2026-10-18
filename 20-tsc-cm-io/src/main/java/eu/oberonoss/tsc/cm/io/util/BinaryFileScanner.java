package eu.oberonoss.tsc.cm.io.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Scans a directory for files that are Binary SERZ files.
 * <p>
 * The user can then call {@link #scan(Consumer)} to scan the directory and pass a consumer to handle each file.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Slf4j
public class BinaryFileScanner {
    private final Path startingDirectory;

    /**
     * Creates a new BinaryFileScanner instance.
     *
     * @param startingDirectory The directory to start scanning from.
     *
     * @since 1.0.0
     */
    public BinaryFileScanner(Path startingDirectory) {
        this.startingDirectory = startingDirectory;
    }

    /**
     * Scans the directory for Binary SERZ files and passes each file to the provided consumer.
     *
     * @param consumer The consumer to handle each file.
     *
     * @since 1.0.0
     */
    public void scan(Consumer<File> consumer) {
        traverse(consumer, startingDirectory.toFile());
    }

    private final FileFilter fileFilter = pathname -> {
        if (pathname.isDirectory()) {
            return true;
        }
        try {
            Path path = pathname.toPath();
            return SerzFileTypeDetector.SERZ_BINARY_CONTENT_TYPE.equalsIgnoreCase(Files.probeContentType(path));
        } catch (IOException _) {
            return false;
        }
    };

    /**
     * Scans the directory for Binary SERZ files and passes each file to the provided consumer.
     *
     * @param consumer          The consumer to handle each file.
     * @param startingDirectory The directory to start scanning from.
     *
     * @since 1.0.0
     */
    public void traverse(Consumer<File> consumer, File startingDirectory) {
        for (File file : Objects.requireNonNull(startingDirectory.listFiles(fileFilter))) {
            if (file.isDirectory()) {
                traverse(consumer, file);
            } else {
                try {
                    if (SerzFileTypeDetector.SERZ_BINARY_CONTENT_TYPE.equalsIgnoreCase(Files.probeContentType(file.toPath()))) {
                        consumer.accept(file);
                    }
                } catch (IOException e) {
                    LOGGER.error("Error scanning file: {}", file.getName(), e);
                }
            }
        }
    }
}
