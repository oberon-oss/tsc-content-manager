package eu.oberonoss.tsc.cm.db.setup;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for the embedded Apache Derby database.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Getter
@Builder(toBuilder = true)
@ToString
public class DatabaseConfig {

    /**
     * Default name of the embedded database.
     *
     * @since 1.0.0
     */
    public static final String DEFAULT_DB_NAME = "tsc-cm-db";

    /**
     * Default path to the master Liquibase changelog file.
     *
     * @since 1.0.0
     */
    public static final String DEFAULT_CHANGELOG_PATH = "db/changelog/db.changelog-master.xml";

    @Builder.Default
    private final String dbName = DEFAULT_DB_NAME;

    @Builder.Default
    private final Path dbDirectory = Paths.get(System.getProperty("user.home"), ".tsc-content-manager", "db");

    @Builder.Default
    private final boolean inMemory = false;

    @Builder.Default
    private final boolean create = true;

    @Builder.Default
    private final boolean autoMigrate = true;

    @Builder.Default
    private final String changelogPath = DEFAULT_CHANGELOG_PATH;

    /**
     * Creates a default file-based database configuration stored in the user home directory.
     *
     * @return A DatabaseConfig instance with default values.
     *
     * @since 1.0.0
     */
    public static DatabaseConfig createDefault() {
        return DatabaseConfig.builder().build();
    }

    /**
     * Creates an in-memory database configuration with the given database name.
     *
     * @param dbName The name of the in-memory database.
     *
     * @return A DatabaseConfig instance configured for in-memory execution.
     *
     * @since 1.0.0
     */
    public static DatabaseConfig createInMemory(String dbName) {
        return DatabaseConfig.builder()
                .dbName(dbName)
                .inMemory(true)
                .build();
    }

    /**
     * Creates an in-memory database configuration with a generated unique name.
     *
     * @return A DatabaseConfig instance configured for in-memory execution.
     *
     * @since 1.0.0
     */
    public static DatabaseConfig createInMemory() {
        return createInMemory("tsc-cm-mem-" + System.nanoTime());
    }

    /**
     * Creates a file-based database configuration in the specified directory.
     *
     * @param directory The directory where the database files should reside.
     * @param dbName    The name of the database.
     *
     * @return A DatabaseConfig instance configured for the specified directory.
     *
     * @since 1.0.0
     */
    public static DatabaseConfig createInDirectory(Path directory, String dbName) {
        return DatabaseConfig.builder()
                .dbDirectory(directory)
                .dbName(dbName)
                .inMemory(false)
                .build();
    }

    /**
     * Constructs the JDBC URL for connecting to the embedded Derby database.
     *
     * @return The JDBC connection URL.
     *
     * @since 1.0.0
     */
    public String getJdbcUrl() {
        StringBuilder url = new StringBuilder("jdbc:derby:");
        if (inMemory) {
            url.append("memory:").append(dbName);
        } else if (dbDirectory != null) {
            url.append(dbDirectory.resolve(dbName).toAbsolutePath());
        } else {
            url.append(dbName);
        }

        if (create) {
            url.append(";create=true");
        }
        return url.toString();
    }

    /**
     * Constructs the JDBC URL for shutting down or dropping the specific database instance.
     *
     * @return The JDBC shutdown URL.
     *
     * @since 1.0.0
     */
    public String getShutdownUrl() {
        StringBuilder url = new StringBuilder("jdbc:derby:");
        if (inMemory) {
            url.append("memory:").append(dbName).append(";drop=true");
        } else if (dbDirectory != null) {
            url.append(dbDirectory.resolve(dbName).toAbsolutePath()).append(";shutdown=true");
        } else {
            url.append(dbName).append(";shutdown=true");
        }
        return url.toString();
    }
}
