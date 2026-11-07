package eu.oberon.oss.tsc.cm.cli.tools;

import eu.oberon.oss.tsc.cm.cli.ToolNameConverter;
import eu.oberon.oss.tsc.cm.cli.ToolRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class LiquibaseMigrationCLITest {

    @BeforeEach
    @AfterEach
    void cleanUp() {
        ToolNameConverter.reset();
    }

    @Test
    void testExecutionInMemory() {
        LiquibaseMigrationCLI cli = new LiquibaseMigrationCLI();
        CommandLine cmd = new CommandLine(cli);

        int exitCode = cmd.execute("-m", "-n", "mem-test-" + System.nanoTime());

        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionInDirectory(@TempDir Path tempDir) throws SQLException {
        LiquibaseMigrationCLI cli = new LiquibaseMigrationCLI();
        CommandLine cmd = new CommandLine(cli);

        int exitCode = cmd.execute("-d", tempDir.toString(), "-n", "test-db");

        assertEquals(0, exitCode);

        // Verify Derby database was created and migrations were applied
        String jdbcUrl = "jdbc:derby:" + tempDir.resolve("test-db").toAbsolutePath();
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM DATABASECHANGELOG")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 2);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM tsc_setting")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }

            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM tsc_scanned_file")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
        } finally {
            try {
                DriverManager.getConnection(jdbcUrl + ";shutdown=true");
            } catch (SQLException _) {
                // Expected on Derby shutdown
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-m",
            "-m -c db/changelog/db.changelog-master.xml",
            "--in-memory --changelog=db/changelog/db.changelog-master.xml",
            "-m --contexts test-ctx --labels test-lbl",
            "-m --db-name test-db-custom"
    })
    void testExecutionWithVariousOptions(String commandLineArgs) {
        LiquibaseMigrationCLI cli = new LiquibaseMigrationCLI();
        CommandLine cmd = new CommandLine(cli);

        String[] args = commandLineArgs.split(" ");
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionWithInvalidChangelogFails() {
        LiquibaseMigrationCLI cli = new LiquibaseMigrationCLI();
        CommandLine cmd = new CommandLine(cli);

        int exitCode = cmd.execute("-m", "-c", "non-existent-changelog.xml");

        assertNotEquals(0, exitCode);
    }

    @Test
    void testHelpOption() {
        LiquibaseMigrationCLI cli = new LiquibaseMigrationCLI();
        CommandLine cmd = new CommandLine(cli);

        int exitCode = cmd.execute("--help");

        assertEquals(0, exitCode);
    }

    @Test
    void testToolRunnerSubcommandExecution() {
        CommandLine cmd = ToolRunner.createCommandLine();

        int exitCode = cmd.execute("liquibase-migration", "-m", "-n", "mem-runner-" + System.nanoTime());

        assertEquals(0, exitCode);
        assertEquals("liquibase-migration", ToolNameConverter.getToolName());
    }

    @Test
    void testCommandAnnotation() {
        CommandLine.Command annotation = LiquibaseMigrationCLI.class.getAnnotation(CommandLine.Command.class);
        assertNotNull(annotation);
        assertEquals("liquibase-migration", annotation.name());
        assertArrayEquals(new String[]{}, annotation.aliases());
        assertTrue(annotation.mixinStandardHelpOptions());
    }

    @Test
    void testLoggerTopic() throws NoSuchFieldException, IllegalAccessException {
        Field loggerField = LiquibaseMigrationCLI.class.getDeclaredField("LOGGER");
        loggerField.setAccessible(true);
        org.slf4j.Logger logger = (org.slf4j.Logger) loggerField.get(null);
        assertEquals("liquibase-migration", logger.getName());
    }
}
