package eu.oberonoss.tsc.cm.db.testutil;

import eu.oberonoss.tsc.cm.db.setup.DatabaseConfig;
import eu.oberonoss.tsc.cm.db.setup.DatabaseManager;
import eu.oberonoss.tsc.cm.db.setup.jpa.JpaManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base test class providing a shared, containerized Derby database instance across tests and modules.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public abstract class AbstractDatabaseTest {

    protected static DatabaseConfig sharedConfig;
    protected static DatabaseManager sharedDatabaseManager;
    protected static JpaManager sharedJpaManager;

    @BeforeAll
    public static synchronized void initSharedDatabase() {
        if (sharedConfig == null) {
            sharedConfig = SharedDerbyContainer.createSharedDatabaseConfig();
            sharedDatabaseManager = new DatabaseManager(sharedConfig);
            sharedDatabaseManager.initialize();
            sharedJpaManager = sharedDatabaseManager.createJpaManager();
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    @AfterEach
    public void cleanupDatabaseTables() {
        if (sharedDatabaseManager != null && sharedDatabaseManager.isInitialized()) {
            try (Connection conn = sharedDatabaseManager.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM tsc_setting");
                stmt.executeUpdate("DELETE FROM tsc_scanned_file");
            } catch (SQLException _) {
                // Ignore if tables are not yet present or already clean
            }
        }
    }
}
