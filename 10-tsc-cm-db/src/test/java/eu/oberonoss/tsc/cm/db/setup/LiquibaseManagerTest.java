package eu.oberonoss.tsc.cm.db.setup;

import eu.oberonoss.tsc.cm.db.DatabaseException;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("SqlNoDataSourceInspection")
class LiquibaseManagerTest {

    private String dbName;
    private EmbeddedDataSource dataSource;
    private LiquibaseManager liquibaseManager;

    @BeforeEach
    void setUp() {
        dbName = "test-liquibase-" + System.nanoTime();
        dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:" + dbName);
        dataSource.setCreateDatabase("create");
        liquibaseManager = new LiquibaseManager();
    }

    @AfterEach
    void tearDown() {
        try {
            java.sql.DriverManager.getConnection("jdbc:derby:memory:" + dbName + ";drop=true");
        } catch (SQLException _) {
            // Expected for Derby drop database
        }
    }

    @Test
    void testRunMigrationsOnConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            liquibaseManager.runMigrations(connection, DatabaseConfig.DEFAULT_CHANGELOG_PATH);

            try (Statement stmt = connection.createStatement()) {
                // Verify DATABASECHANGELOG table exists and has entries
                try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM DATABASECHANGELOG")) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(2);
                }

                // Verify tables created by schema migrations exist
                try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM tsc_setting")) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isZero();
                }

                try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM tsc_scanned_file")) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isZero();
                }
            }
        }
    }

    @Test
    void testRunMigrationsOnDataSource() throws SQLException {
        liquibaseManager.runMigrations(dataSource, DatabaseConfig.DEFAULT_CHANGELOG_PATH);

        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM tsc_setting")) {
            assertThat(rs.next()).isTrue();
        }
    }

    @Test
    void testRunMigrationsIsIdempotent() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            liquibaseManager.runMigrations(connection, DatabaseConfig.DEFAULT_CHANGELOG_PATH);
            // Running again should not fail
            liquibaseManager.runMigrations(connection, DatabaseConfig.DEFAULT_CHANGELOG_PATH);

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM DATABASECHANGELOG")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(2);
            }
        }
    }

    @Test
    void testHasPendingMigrations() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // Before migrations run, pending migrations should be true
            assertThat(liquibaseManager.hasPendingMigrations(connection, DatabaseConfig.DEFAULT_CHANGELOG_PATH)).isTrue();

            // Run migrations
            liquibaseManager.runMigrations(connection, DatabaseConfig.DEFAULT_CHANGELOG_PATH);

            // After migrations run, pending migrations should be false
            assertThat(liquibaseManager.hasPendingMigrations(connection, DatabaseConfig.DEFAULT_CHANGELOG_PATH)).isFalse();
        }
    }

    @Test
    void testInvalidChangelogThrowsException() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThatThrownBy(() -> liquibaseManager.runMigrations(connection, "non-existent-changelog.xml"))
                    .isInstanceOf(DatabaseException.class)
                    .hasMessageContaining("Failed to run Liquibase migrations");
        }
    }
}
