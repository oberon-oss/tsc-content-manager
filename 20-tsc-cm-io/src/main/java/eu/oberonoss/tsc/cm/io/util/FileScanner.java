package eu.oberonoss.tsc.cm.io.util;

import java.io.IOException;
import java.nio.file.Path;

/**
 * An interface describing the contract for scanning a directory or file.
 * <p>
 * The user can then call {@link FileScanner#scan(FileProcessor, Path)} to scan the directory and pass a consumer to handle each file.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface FileScanner {

    int DEFAULT_THREAD_COUNT = 4;

    /**
     * Scans the directory for files matching the processor, loads each file's data, and passes it to the provided consumer using the scanner's configured
     * thread count.
     *
     * @param <T>             The type of loaded data produced by the file processor
     * @param processor       The processor for the scan operation. It contains the file filter for selecting the file or files
     * @param fileOrDirectory A single file or directory to start scanning from.
     *
     * @since 1.0.0
     */
    default <T> void scan(FileProcessor<T> processor, Path fileOrDirectory) {
        scan(processor, fileOrDirectory, threadCount());
    }

    /**
     * Scans the directory for files matching the processor, loads each file's data, and passes it to the provided consumer using the specified number of worker
     * threads.
     *
     * @param <T>             The type of loaded data produced by the file processor
     * @param processor       The processor for the scan operation. It contains the file filter for selecting the file or files
     * @param fileOrDirectory A single file or directory to start scanning from.
     * @param threadCount     The number of worker threads to use for parallel scanning. If the specified value is less than or equal to zero, a default thread
     *                        count of 1 (one) will be used.
     *
     * @since 1.0.0
     */
    <T> void scan(FileProcessor<T> processor, Path fileOrDirectory, int threadCount);

    /**
     * Gets the configured thread count for this scanner.
     *
     * @return The thread count
     *
     * @since 1.0.0
     */
    default int threadCount() {
        return DEFAULT_THREAD_COUNT;
    }

    /**
     * Loads the data for a given file based on the processor's data type.
     *
     * @param <T>       The type of loaded data
     * @param processor The file processor
     * @param path      The path to the file to load
     *
     * @return The loaded data
     *
     * @throws IOException If an I/O error occurs, loading the file
     * @since 1.0.0
     */
    <T> T loadData(FileProcessor<T> processor, Path path) throws IOException;

    /**
     * Creates a new FileScanner instance with default thread count.
     *
     * @return A new FileScanner instance
     *
     * @since 1.0.0
     */
    static FileScanner of() {
        return new FileScannerImpl();
    }

    /**
     * Creates a new FileScanner instance with the specified thread count.
     *
     * @param threadCount The number of worker threads to use
     *
     * @return A new FileScanner instance
     *
     * @since 1.0.0
     */
    static FileScanner of(int threadCount) {
        return new FileScannerImpl(threadCount);
    }
}
