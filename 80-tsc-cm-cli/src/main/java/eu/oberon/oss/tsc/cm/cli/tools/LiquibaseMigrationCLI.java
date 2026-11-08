package eu.oberon.oss.tsc.cm.cli.tools;

import eu.oberon.oss.tsc.cm.cli.ToolNameConverter;
import eu.oberonoss.tsc.cm.db.setup.DatabaseConfig;
import eu.oberonoss.tsc.cm.db.setup.DatabaseManager;
import eu.oberonoss.tsc.cm.db.setup.LiquibaseManager;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.concurrent.Callable;

/**
 * Command line tool to execute Liquibase database migrations for TSC Content Manager.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Command(
        name = "liquibase-migration",
        description = "Executes Liquibase database migrations for TSC Content Manager",
        mixinStandardHelpOptions = true
)
@Slf4j(topic = "liquibase-migration")
public class LiquibaseMigrationCLI implements Callable<Integer> {

    @Option(
            names = {"--db-directory", "-d"},
            description = "Directory where the database files reside (default: <app.home>/db or ~/.tsc-content-manager/db)"
    )
    Path dbDirectory;

    @Option(
            names = {"--db-name", "-n"},
            description = "Name of the database",
            defaultValue = DatabaseConfig.DEFAULT_DB_NAME
    )
    String dbName = DatabaseConfig.DEFAULT_DB_NAME;

    @Option(
            names = {"--changelog", "-c"},
            description = "Path to the master Liquibase changelog resource file",
            defaultValue = DatabaseConfig.DEFAULT_CHANGELOG_PATH
    )
    String changelogPath = DatabaseConfig.DEFAULT_CHANGELOG_PATH;

    @Option(
            names = {"--in-memory", "-m"},
            description = "Run migrations against an in-memory database instead of file-based",
            defaultValue = "false"
    )
    boolean inMemory;

    @Option(
            names = {"--host", "-H"},
            description = "Host name or IP address of the remote Derby Network Server"
    )
    String host;

    @Option(
            names = {"--port", "-P"},
            description = "Port number of the remote Derby Network Server"
    )
    Integer port;

    @Option(
            names = {"--contexts"},
            description = "Optional Liquibase contexts to filter changesets"
    )
    String contexts;

    @Option(
            names = {"--labels"},
            description = "Optional Liquibase labels to filter changesets"
    )
    String labels;

    /**
     * Default constructor for LiquibaseMigrationCLI.
     *
     * @since 1.0.0
     */
    public LiquibaseMigrationCLI() {
        // Keep Javadoc happy
    }

    /**
     * Entry point for running the Liquibase migration CLI tool directly.
     *
     * @param args Command line arguments.
     * @since 1.0.0
     */
    static void main(String[] args) {
        CommandLine cmd = new CommandLine(new LiquibaseMigrationCLI());
        cmd.setExecutionStrategy(parseResult -> {
            ToolNameConverter.setToolName(LiquibaseMigrationCLI.class);
            return new CommandLine.RunLast().execute(parseResult);
        });
        System.exit(cmd.execute(args));
    }

    /**
     * Executes the Liquibase migration process.
     *
     * @return The exit code (0 for success, non-zero for failure).
     * @since 1.0.0
     */
    @Override
    public Integer call() {
        ToolNameConverter.setToolName(getClass());
        LOGGER.info("Starting Liquibase database migration process...");

        DatabaseConfig.DatabaseConfigBuilder configBuilder = DatabaseConfig.builder()
                .dbName(dbName)
                .changelogPath(changelogPath)
                .inMemory(inMemory)
                .autoMigrate(false)
                .create(true);

        if (dbDirectory != null) {
            configBuilder.dbDirectory(dbDirectory);
        }
        if (host != null && !host.isBlank()) {
            configBuilder.host(host);
        }
        if (port != null && port > 0) {
            configBuilder.port(port);
        }

        DatabaseConfig config = configBuilder.build();
        LOGGER.info("Target database configuration: dbName={}, inMemory={}, host={}, port={}, dbDirectory={}",
                config.getDbName(), config.isInMemory(), config.getHost(), config.getPort(), config.getDbDirectory());
        LOGGER.info("Using master changelog: {}", config.getChangelogPath());

        LiquibaseManager liquibaseManager = new LiquibaseManager();
        try (DatabaseManager databaseManager = new DatabaseManager(config, liquibaseManager)) {
            databaseManager.initialize();
            try (Connection connection = databaseManager.getConnection()) {
                liquibaseManager.runMigrations(connection, config.getChangelogPath(), contexts, labels);
            }
            LOGGER.info("Liquibase database migration completed successfully.");
            return 0;
        } catch (Exception e) {
            LOGGER.error("Liquibase database migration failed: {}", e.getMessage(), e);
            return 1;
        }
    }
}
