package eu.oberonoss.tsc.cm.db.setup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseIntegrationTest {

    @Test
    void testDataPersistenceAcrossRestarts(@TempDir Path tempDir) throws SQLException {
        DatabaseConfig config = DatabaseConfig.createInDirectory(tempDir, "integration-test-db");

        // First session: create and insert data
        try (DatabaseManager manager1 = new DatabaseManager(config)) {
            manager1.initialize();

            try (Connection conn = manager1.getConnection()) {
                // Insert setting
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO tsc_setting (setting_key, setting_value, description, updated_at) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, "app.version");
                    ps.setString(2, "1.0.0");
                    ps.setString(3, "Application version");
                    ps.setTimestamp(4, Timestamp.from(Instant.now()));
                    int rows = ps.executeUpdate();
                    assertThat(rows).isEqualTo(1);
                }

                // Insert scanned file
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO tsc_scanned_file (file_path, file_name, file_size, file_type, last_modified) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, "/trains/route.bin");
                    ps.setString(2, "route.bin");
                    ps.setLong(3, 102400L);
                    ps.setString(4, "SERZ");
                    ps.setTimestamp(5, Timestamp.from(Instant.now()));
                    int rows = ps.executeUpdate();
                    assertThat(rows).isEqualTo(1);
                }
            }
        }

        // Second session: re-open database from disk and verify data persisted
        try (DatabaseManager manager2 = new DatabaseManager(config)) {
            manager2.initialize();

            try (Connection conn = manager2.getConnection()) {
                // Verify setting
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT setting_value, description FROM tsc_setting WHERE setting_key = ?")) {
                    ps.setString(1, "app.version");
                    try (ResultSet rs = ps.executeQuery()) {
                        assertThat(rs.next()).isTrue();
                        assertThat(rs.getString("setting_value")).isEqualTo("1.0.0");
                        assertThat(rs.getString("description")).isEqualTo("Application version");
                    }
                }

                // Verify scanned file
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, file_name, file_size FROM tsc_scanned_file WHERE file_path = ?")) {
                    ps.setString(1, "/trains/route.bin");
                    try (ResultSet rs = ps.executeQuery()) {
                        assertThat(rs.next()).isTrue();
                        assertThat(rs.getLong("id")).isPositive();
                        assertThat(rs.getString("file_name")).isEqualTo("route.bin");
                        assertThat(rs.getLong("file_size")).isEqualTo(102400L);
                    }
                }
            }
        }
    }
}
