package eu.oberonoss.tsc.cm.db.setup;

import eu.oberonoss.tsc.cm.db.DatabaseException;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages Liquibase database migrations for schema creation and updates.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class LiquibaseManager {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseManager.class);

    /**
     * Executes Liquibase migrations on the given connection using the specified changelog.
     *
     * @param connection    The active database connection.
     * @param changelogPath The resource path to the master changelog file.
     *
     * @throws DatabaseException If an error occurs during migration.
     * @since 1.0.0
     */
    public void runMigrations(Connection connection, String changelogPath) {
        runMigrations(connection, changelogPath, null, null);
    }

    /**
     * Executes Liquibase migrations on the given connection with optional contexts and labels.
     *
     * @param connection    The active database connection.
     * @param changelogPath The resource path to the master changelog file.
     * @param contexts      Optional Liquibase contexts ({@code <null>} is allowed).
     * @param labels        Optional Liquibase labels ({@code <null>} is allowed).
     *
     * @throws DatabaseException If an error occurs during migration.
     * @since 1.0.0
     */
    public void runMigrations(Connection connection, String changelogPath, String contexts, String labels) {
        log.info("Starting Liquibase migration using changelog: {}", changelogPath);
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
        } catch (SQLException _) {
            // Ignore if unable to read autoCommit status
        }

        try (Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection) {
                    @Override
                    public void close() {
                        // Do not close caller-managed connection
                    }
                });
             ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
             Liquibase liquibase = new Liquibase(changelogPath, resourceAccessor, database)) {
            if (database instanceof liquibase.database.core.DerbyDatabase derbyDb) {
                derbyDb.setShutdownEmbeddedDerby(false);
            }
            Contexts ctx = contexts != null ? new Contexts(contexts) : new Contexts();
            LabelExpression lbl = labels != null ? new LabelExpression(labels) : new LabelExpression();
            liquibase.update(ctx, lbl);

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            log.info("Liquibase migration completed successfully.");
        } catch (Exception e) {
            log.error("Liquibase migration failed for changelog: {}", changelogPath, e);
            try {
                if (!connection.isClosed() && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                log.warn("Failed to rollback connection after migration failure", rollbackEx);
            }
            throw new DatabaseException("Failed to run Liquibase migrations: " + e.getMessage(), e);
        } finally {
            try {
                if (!connection.isClosed()) {
                    connection.setAutoCommit(originalAutoCommit);
                }
            } catch (SQLException _) {
                // Ignore failure to restore autoCommit
            }
        }
    }

    /**
     * Executes Liquibase migrations against a DataSource.
     *
     * @param dataSource    The DataSource to obtain a connection from.
     * @param changelogPath The resource path to the master changelog file.
     *
     * @throws DatabaseException If an error occurs during migration.
     * @since 1.0.0
     */
    public void runMigrations(DataSource dataSource, String changelogPath) {
        try (Connection connection = dataSource.getConnection()) {
            runMigrations(connection, changelogPath);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to obtain database connection for migrations: " + e.getMessage(), e);
        }
    }
}
