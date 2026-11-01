package eu.oberonoss.tsc.cm.db;

/**
 * Runtime exception for database operations and migrations.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class DatabaseException extends RuntimeException {

    /**
     * Constructs a new DatabaseException with the specified detail message.
     *
     * @param message The detail message.
     *
     * @since 1.0.0
     */
    public DatabaseException(String message) {
        super(message);
    }

    /**
     * Constructs a new DatabaseException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause.
     *
     * @since 1.0.0
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new DatabaseException with the specified cause.
     *
     * @param cause The cause.
     *
     * @since 1.0.0
     */
    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
