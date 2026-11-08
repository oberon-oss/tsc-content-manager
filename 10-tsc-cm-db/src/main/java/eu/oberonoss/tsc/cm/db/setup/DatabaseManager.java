package eu.oberonoss.tsc.cm.db.setup;

import eu.oberonoss.tsc.cm.db.DatabaseException;
import eu.oberonoss.tsc.cm.db.setup.jpa.JpaManager;
import lombok.Getter;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the embedded Apache Derby database lifecycle and connections.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class DatabaseManager implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private static final String DERBY_SHUTDOWN_SINGLE_DB_SQL_STATE = "08006";
    private static final String DERBY_SHUTDOWN_SYSTEM_SQL_STATE = "XJ015";
    private static final String DERBY_DATABASE_NOT_FOUND_SQL_STATE = "08004";

    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            log.warn("Derby EmbeddedDriver not found on classpath", e);
        }
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
        } catch (ClassNotFoundException e) {
            log.debug("Derby ClientDriver not found on classpath", e);
        }
    }

    //@formatter:off
    /**
     * The database configuration.
     * --- GETTER ---
     * @return The DatabaseConfig instance.
     *
     * @since 1.0.0
     */
    //@formatter:on
    @SuppressWarnings({"JavadocDeclaration"})
    @Getter
    private final DatabaseConfig config;

    private final LiquibaseManager liquibaseManager;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private DataSource dataSource;

    /**
     * Creates a new DatabaseManager with the provided configuration.
     *
     * @param config The database configuration.
     *
     * @since 1.0.0
     */
    public DatabaseManager(DatabaseConfig config) {
        this(config, new LiquibaseManager());
    }

    /**
     * Creates a new DatabaseManager with the provided configuration and LiquibaseManager.
     *
     * @param config           The database configuration.
     * @param liquibaseManager The Liquibase migration manager.
     *
     * @since 1.0.0
     */
    public DatabaseManager(DatabaseConfig config, LiquibaseManager liquibaseManager) {
        this.config = config != null ? config : DatabaseConfig.createDefault();
        this.liquibaseManager = liquibaseManager != null ? liquibaseManager : new LiquibaseManager();
    }

    /**
     * Initializes the embedded Derby database and runs Liquibase migrations if configured.
     *
     * @throws DatabaseException If initialization or migration fails.
     * @since 1.0.0
     */
    public synchronized void initialize() {
        if (closed.get()) {
            throw new DatabaseException("Cannot initialize closed DatabaseManager.");
        }
        if (initialized.get()) {
            log.debug("DatabaseManager is already initialized.");
            return;
        }

        log.info("Initializing Derby database with URL: {}", config.getJdbcUrl());
        try {
            DataSource ds;
            if (config.isRemote()) {
                org.apache.derby.jdbc.ClientDataSource clientDs = new org.apache.derby.jdbc.ClientDataSource();
                clientDs.setServerName(config.getHost());
                clientDs.setPortNumber(config.getPort());
                clientDs.setDatabaseName(config.getDbName());
                if (config.isCreate()) {
                    clientDs.setCreateDatabase("create");
                }
                ds = clientDs;
            } else {
                EmbeddedDataSource embeddedDs = new EmbeddedDataSource();
                if (config.isInMemory()) {
                    embeddedDs.setDatabaseName("memory:" + config.getDbName());
                } else if (config.getDbDirectory() != null) {
                    embeddedDs.setDatabaseName(config.getDbDirectory().resolve(config.getDbName()).toAbsolutePath().toString());
                } else {
                    embeddedDs.setDatabaseName(config.getDbName());
                }

                if (config.isCreate()) {
                    embeddedDs.setCreateDatabase("create");
                }
                ds = embeddedDs;
            }

            this.dataSource = ds;

            // Verify connection and run initial migrations if autoMigrate is enabled
            try (Connection conn = dataSource.getConnection()) {
                log.info("Successfully connected to Derby DB: {}", conn.getMetaData().getDatabaseProductName());
                if (config.isAutoMigrate()) {
                    liquibaseManager.runMigrations(conn, config.getChangelogPath());
                }
            }

            initialized.set(true);
        } catch (SQLException e) {
            log.error("Failed to initialize Derby database", e);
            throw new DatabaseException("Failed to initialize Derby database: " + e.getMessage(), e);
        }
    }

    /**
     * Runs database migrations using the configured changelog.
     *
     * @throws DatabaseException If migration fails.
     * @since 1.0.0
     */
    public void migrate() {
        ensureOpenAndInitialized();
        try (Connection conn = getConnection()) {
            liquibaseManager.runMigrations(conn, config.getChangelogPath());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to run migrations: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a connection to the embedded Derby database.
     *
     * @return An active Connection.
     *
     * @throws SQLException If a database access error occurs.
     * @since 1.0.0
     */
    public Connection getConnection() throws SQLException {
        ensureOpenAndInitialized();
        return dataSource.getConnection();
    }

    /**
     * Returns the underlying DataSource.
     *
     * @return The EmbeddedDataSource instance.
     *
     * @since 1.0.0
     */
    public DataSource getDataSource() {
        ensureOpenAndInitialized();
        return dataSource;
    }

    /**
     * Creates a JpaManager bound to this DatabaseManager's DataSource.
     *
     * @return A configured JpaManager.
     *
     * @since 1.0.0
     */
    public JpaManager createJpaManager() {
        ensureOpenAndInitialized();
        return JpaManager.create(this);
    }

    /**
     * Checks whether the database manager has been initialized.
     *
     * @return True if initialized, false otherwise.
     *
     * @since 1.0.0
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Checks whether the database manager is closed.
     *
     * @return True if closed, false otherwise.
     *
     * @since 1.0.0
     */
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * Shuts down the embedded Derby database instance.
     *
     * @since 1.0.0
     */
    public synchronized void shutdown() {
        if (closed.get()) {
            return;
        }

        if (config.isRemote()) {
            log.info("Closing remote Derby database manager connection: {}", config.getDbName());
            closed.set(true);
            initialized.set(false);
            return;
        }

        log.info("Shutting down embedded Derby database: {}", config.getDbName());
        try {
            EmbeddedDataSource ds = new EmbeddedDataSource();
            if (config.isInMemory()) {
                ds.setDatabaseName("memory:" + config.getDbName());
                ds.setConnectionAttributes("drop=true");
            } else if (config.getDbDirectory() != null) {
                ds.setDatabaseName(config.getDbDirectory().resolve(config.getDbName()).toAbsolutePath().toString());
                ds.setShutdownDatabase("shutdown");
            } else {
                ds.setDatabaseName(config.getDbName());
                ds.setShutdownDatabase("shutdown");
            }
            ds.getConnection();
        } catch (SQLException e) {
            if (isExpectedShutdownException(e)) {
                log.info("Embedded Derby database shut down successfully: {}", e.getMessage());
            } else {
                log.warn("Unexpected exception during Derby shutdown: {}", e.getMessage(), e);
            }
        } finally {
            closed.set(true);
            initialized.set(false);
        }
    }

    /**
     * Shuts down the entire Apache Derby engine.
     *
     * @since 1.0.0
     */
    public static void shutdownDerbyEngine() {
        log.info("Shutting down Apache Derby engine.");
        try {
            new org.apache.derby.jdbc.EmbeddedDriver();
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if (DERBY_SHUTDOWN_SYSTEM_SQL_STATE.equals(e.getSQLState()) || DERBY_SHUTDOWN_SINGLE_DB_SQL_STATE.equals(e.getSQLState())) {
                log.info("Apache Derby engine shut down successfully.");
            } else {
                log.warn("Unexpected exception during Derby engine shutdown: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    private void ensureOpenAndInitialized() {
        if (closed.get()) {
            throw new DatabaseException("DatabaseManager is closed.");
        }
        if (!initialized.get()) {
            initialize();
        }
    }

    private boolean isExpectedShutdownException(SQLException e) {
        String sqlState = e.getSQLState();
        return DERBY_SHUTDOWN_SINGLE_DB_SQL_STATE.equals(sqlState)
                || DERBY_SHUTDOWN_SYSTEM_SQL_STATE.equals(sqlState)
                || DERBY_DATABASE_NOT_FOUND_SQL_STATE.equals(sqlState);
    }
}
