package eu.oberonoss.tsc.cm.io.util;

import eu.oberonoss.tsc.cm.base.FileDataType;

import java.io.FileFilter;
import java.util.function.Consumer;

/**
 * An interface for processing files during scanning operations.
 *
 * @param <T> The type of loaded data produced for this processor
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface FileProcessor<T> {
    /**
     * Retrieves the type of file data that this processor works with.
     *
     * @return The file data type, represented as a {@link FileDataType} enum value.
     *
     * @since 1.0.0
     */
    FileDataType getFileDataType();

    /**
     * Retrieves the file filter used by this processor to select files for processing.
     *
     * @return The file filter, represented as a {@link FileFilter} object.
     *
     * @since 1.0.0
     */
    FileFilter getFileFilter();

    /**
     * Retrieves the consumer function that processes the loaded data produced by this processor.
     *
     * @return The consumer function, represented as a {@link Consumer} object.
     *
     * @since 1.0.0
     */
    Consumer<T> consumer();
}
