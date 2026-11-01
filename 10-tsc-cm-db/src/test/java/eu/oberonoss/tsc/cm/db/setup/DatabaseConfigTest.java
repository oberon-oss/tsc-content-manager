package eu.oberonoss.tsc.cm.db.setup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseConfigTest {

    @Test
    void testDefaultConfig() {
        DatabaseConfig config = DatabaseConfig.createDefault();

        assertThat(config.getDbName()).isEqualTo(DatabaseConfig.DEFAULT_DB_NAME);
        assertThat(config.getChangelogPath()).isEqualTo(DatabaseConfig.DEFAULT_CHANGELOG_PATH);
        assertThat(config.isInMemory()).isFalse();
        assertThat(config.isCreate()).isTrue();
        assertThat(config.isAutoMigrate()).isTrue();
        assertThat(config.getDbDirectory()).isNotNull();
        assertThat(config.getJdbcUrl()).contains("jdbc:derby:");
        assertThat(config.getJdbcUrl()).contains(";create=true");
        assertThat(config.getShutdownUrl()).contains(";shutdown=true");
    }

    @Test
    void testInMemoryConfig() {
        DatabaseConfig config = DatabaseConfig.createInMemory("my-test-db");

        assertThat(config.getDbName()).isEqualTo("my-test-db");
        assertThat(config.isInMemory()).isTrue();
        assertThat(config.isCreate()).isTrue();
        assertThat(config.isAutoMigrate()).isTrue();
        assertThat(config.getJdbcUrl()).isEqualTo("jdbc:derby:memory:my-test-db;create=true");
        assertThat(config.getShutdownUrl()).isEqualTo("jdbc:derby:memory:my-test-db;drop=true");
    }

    @Test
    void testInMemoryGeneratedName() {
        DatabaseConfig config = DatabaseConfig.createInMemory();

        assertThat(config.getDbName()).startsWith("tsc-cm-mem-");
        assertThat(config.isInMemory()).isTrue();
        assertThat(config.getJdbcUrl()).startsWith("jdbc:derby:memory:tsc-cm-mem-");
    }

    @Test
    void testInDirectoryConfig(@TempDir Path tempDir) {
        DatabaseConfig config = DatabaseConfig.createInDirectory(tempDir, "custom-db");

        assertThat(config.getDbName()).isEqualTo("custom-db");
        assertThat(config.getDbDirectory()).isEqualTo(tempDir);
        assertThat(config.isInMemory()).isFalse();
        assertThat(config.getJdbcUrl()).isEqualTo("jdbc:derby:" + tempDir.resolve("custom-db").toAbsolutePath() + ";create=true");
        assertThat(config.getShutdownUrl()).isEqualTo("jdbc:derby:" + tempDir.resolve("custom-db").toAbsolutePath() + ";shutdown=true");
    }

    @Test
    void testCustomBuilder() {
        DatabaseConfig config = DatabaseConfig.builder()
                .dbName("custom")
                .dbDirectory(null)
                .inMemory(false)
                .create(false)
                .autoMigrate(false)
                .changelogPath("custom/changelog.xml")
                .build();

        assertThat(config.getDbName()).isEqualTo("custom");
        assertThat(config.getDbDirectory()).isNull();
        assertThat(config.isCreate()).isFalse();
        assertThat(config.isAutoMigrate()).isFalse();
        assertThat(config.getChangelogPath()).isEqualTo("custom/changelog.xml");
        assertThat(config.getJdbcUrl()).isEqualTo("jdbc:derby:custom");
        assertThat(config.getShutdownUrl()).isEqualTo("jdbc:derby:custom;shutdown=true");
    }

    @Test
    void testResolveDefaultDbDirectoryWithAppHomeProperty(@TempDir Path tempDir) {
        String originalAppHome = System.getProperty("app.home");
        try {
            System.setProperty("app.home", tempDir.toString());
            Path resolved = DatabaseConfig.resolveDefaultDbDirectory();
            assertThat(resolved).isEqualTo(tempDir.resolve("db"));

            DatabaseConfig config = DatabaseConfig.createDefault();
            assertThat(config.getDbDirectory()).isEqualTo(tempDir.resolve("db"));
        } finally {
            if (originalAppHome != null) {
                System.setProperty("app.home", originalAppHome);
            } else {
                System.clearProperty("app.home");
            }
        }
    }

    @Test
    void testResolveDefaultDbDirectoryFallback() {
        String originalAppHome = System.getProperty("app.home");
        try {
            System.clearProperty("app.home");
            Path resolved = DatabaseConfig.resolveDefaultDbDirectory();
            assertThat(resolved).isNotNull();
            assertThat(resolved).endsWith(java.nio.file.Paths.get(".tsc-content-manager", "db"));
        } finally {
            if (originalAppHome != null) {
                System.setProperty("app.home", originalAppHome);
            }
        }
    }

    @Test
    void testToStringAndToBuilder() {
        DatabaseConfig config = DatabaseConfig.createInMemory("test-db");
        DatabaseConfig modified = config.toBuilder().autoMigrate(false).build();

        assertThat(config.toString()).contains("test-db");
        assertThat(modified.isAutoMigrate()).isFalse();
        assertThat(modified.getDbName()).isEqualTo("test-db");
    }
}
