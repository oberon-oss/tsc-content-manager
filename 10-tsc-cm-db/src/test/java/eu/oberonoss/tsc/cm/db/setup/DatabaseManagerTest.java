package eu.oberonoss.tsc.cm.db.setup;

import eu.oberonoss.tsc.cm.db.DatabaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DatabaseManagerTest {

    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        DatabaseConfig config = DatabaseConfig.createInMemory("db-mgr-test-" + System.nanoTime());
        databaseManager = new DatabaseManager(config);
    }

    @AfterEach
    void tearDown() {
        if (databaseManager != null && !databaseManager.isClosed()) {
            databaseManager.close();
        }
    }

    @Test
    void testInitializeAndAutoMigrate() throws SQLException {
        assertThat(databaseManager.isInitialized()).isFalse();
        assertThat(databaseManager.isClosed()).isFalse();

        databaseManager.initialize();

        assertThat(databaseManager.isInitialized()).isTrue();
        assertThat(databaseManager.isClosed()).isFalse();
        assertThat(databaseManager.getDataSource()).isNotNull();

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM tsc_setting")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isZero();
        }
    }

    @Test
    void testMultipleInitializeCallsAreIdempotent() {
        databaseManager.initialize();
        assertThat(databaseManager.isInitialized()).isTrue();

        // Second call should not fail or throw
        databaseManager.initialize();
        assertThat(databaseManager.isInitialized()).isTrue();
    }

    @Test
    void testManualMigrationWhenAutoMigrateDisabled() throws SQLException {
        DatabaseConfig noMigrateConfig = DatabaseConfig.builder()
                .dbName("db-no-migrate-" + System.nanoTime())
                .inMemory(true)
                .autoMigrate(false)
                .build();

        try (DatabaseManager manager = new DatabaseManager(noMigrateConfig)) {
            manager.initialize();

            // Tables should not exist before migrate() is called
            try (Connection conn = manager.getConnection();
                 Statement stmt = conn.createStatement()) {
                assertThatThrownBy(() -> stmt.executeQuery("SELECT count(*) FROM tsc_setting"))
                        .isInstanceOf(SQLException.class);
            }

            // Explicitly call migrate()
            manager.migrate();

            // Now tables should exist
            try (Connection conn = manager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM tsc_setting")) {
                assertThat(rs.next()).isTrue();
            }
        }
    }

    @Test
    void testGetConnectionAutoInitializes() throws SQLException {
        assertThat(databaseManager.isInitialized()).isFalse();

        try (Connection conn = databaseManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(databaseManager.isInitialized()).isTrue();
        }
    }

    @Test
    void testShutdownAndClose() {
        databaseManager.initialize();
        assertThat(databaseManager.isInitialized()).isTrue();
        assertThat(databaseManager.isClosed()).isFalse();

        databaseManager.shutdown();

        assertThat(databaseManager.isInitialized()).isFalse();
        assertThat(databaseManager.isClosed()).isTrue();

        // Calling shutdown again should be a no-op
        databaseManager.shutdown();
        assertThat(databaseManager.isClosed()).isTrue();

        // Using closed manager should throw DatabaseException
        assertThatThrownBy(() -> databaseManager.getConnection())
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("closed");

        assertThatThrownBy(() -> databaseManager.initialize())
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void testShutdownDerbyEngine() {
        assertDoesNotThrow(DatabaseManager::shutdownDerbyEngine);
    }

    @Test
    void testRemoteConfigurationInitializationAndShutdown() {
        DatabaseConfig remoteConfig = DatabaseConfig.builder()
                .host("localhost")
                .port(1527)
                .dbName("remote-test-db")
                .create(true)
                .build();

        try (DatabaseManager manager = new DatabaseManager(remoteConfig)) {
            assertThatThrownBy(manager::initialize)
                    .isInstanceOf(DatabaseException.class)
                    .hasMessageContaining("Failed to initialize Derby database");

            assertThat(manager.isInitialized()).isFalse();
            manager.shutdown();
            assertThat(manager.isClosed()).isTrue();
        }
    }

    @Test
    void testNullDatabaseConfigDefaultsToDefault() {
        try (DatabaseManager manager = new DatabaseManager(null, null)) {
            assertThat(manager.getConfig()).isNotNull();
            assertThat(manager.getConfig().getDbName()).isEqualTo(DatabaseConfig.DEFAULT_DB_NAME);
        }
    }

    @Test
    void testEmbeddedDatabaseWithoutDbDirectory() {
        DatabaseConfig noDirConfig = DatabaseConfig.builder()
                .dbName("nodir-test-" + System.nanoTime())
                .dbDirectory(null)
                .inMemory(true)
                .build();

        try (DatabaseManager manager = new DatabaseManager(noDirConfig)) {
            manager.initialize();
            assertThat(manager.isInitialized()).isTrue();
            assertThat(manager.getDataSource()).isNotNull();
        }
    }

    @Test
    void testEmbeddedDatabaseWithoutDbDirectoryNonMemory(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) {
        String dbPath = tempDir.resolve("nodir-file-" + System.nanoTime()).toAbsolutePath().toString();
        DatabaseConfig noDirConfig = DatabaseConfig.builder()
                .dbName(dbPath)
                .dbDirectory(null)
                .inMemory(false)
                .build();

        try (DatabaseManager manager = new DatabaseManager(noDirConfig)) {
            manager.initialize();
            assertThat(manager.isInitialized()).isTrue();
        }
    }
}
