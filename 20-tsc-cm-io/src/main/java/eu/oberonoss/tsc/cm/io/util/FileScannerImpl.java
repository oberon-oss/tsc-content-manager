package eu.oberonoss.tsc.cm.io.util;

import eu.oberonoss.tsc.cm.base.ByteLoaderImpl;
import eu.oberonoss.tsc.cm.base.FileDataType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Implementation of the {@link FileScanner} interface.
 *
 * @param threadCount The number of threads to use.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public record FileScannerImpl(int threadCount) implements FileScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerImpl.class);

    /**
     * Creates a new FileScannerImpl with the default thread count.
     *
     * @since 1.0.0
     */
    public FileScannerImpl() {
        this(DEFAULT_THREAD_COUNT);
    }

    /**
     * Creates a new FileScannerImpl with the specified thread count.
     *
     * @param threadCount The number of worker threads to use
     *
     * @since 1.0.0
     */
    public FileScannerImpl(int threadCount) {
        this.threadCount = Math.max(1, threadCount);
    }

    @Override
    public <T> void scan(FileProcessor<T> processor, Path fileOrDirectory, int threadCount) {
        if (!Files.exists(fileOrDirectory)) {
            return;
        }

        int threads = Math.max(1, threadCount);
        int queueCapacity = threads * 4;
        try (ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threads,
                threads,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy()
        )) {
            long start = System.currentTimeMillis();
            final LongAdder entriesFound = new LongAdder();
            final LongAdder processedFiles = new LongAdder();

            walkTheFileTree(executor, fileOrDirectory, processor, entriesFound, processedFiles);

            long total = System.currentTimeMillis() - start;
            long seconds = Math.max(1, total / 1000);
            long files = processedFiles.sum();
            long entries = entriesFound.sum();
            LOGGER.info("Processing completed. Found {} entries; processed {} files in {} ms (avg: {} files/s)", entries, files, total, files / seconds);
        } catch (IOException e) {
            throw new RuntimeException("Error scanning path: " + fileOrDirectory, e);
        }
    }

    private <T> void walkTheFileTree(ExecutorService executor, Path fileOrDirectory, FileProcessor<T> processor, LongAdder entriesFound, LongAdder processedFiles) throws IOException {
        Files.walkFileTree(fileOrDirectory, new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult visitFile(@NotNull Path path, @NotNull BasicFileAttributes attrs) {
                File file = path.toFile();
                entriesFound.increment();
                if (processor.getFileFilter() == null || processor.getFileFilter().accept(file)) {
                    executor.execute(() -> {
                        processedFiles.increment();
                        long current = processedFiles.sum();
                        if (current % 5000 == 0) {
                            LOGGER.info("Processed {} files", current);
                        }
                        try {
                            T data = loadData(processor, path);
                            if (data != null) {
                                processor.consumer().accept(data);
                            }
                        } catch (Exception e) {
                            LOGGER.error("Error processing file {}: {}", path, e.getMessage(), e);
                        }
                    });
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult visitFileFailed(@NotNull Path file, @NotNull IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T loadData(FileProcessor<T> processor, Path path) throws IOException {
        if (processor.getFileDataType() == FileDataType.SERZ_BINARY) {
            return (T) ByteLoaderImpl.getInstance().getBinaryDataReader(path);
        }
        return null;
    }
}
