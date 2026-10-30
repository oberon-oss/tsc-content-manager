package eu.oberon.oss.tsc.cm.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ToolRunnerTest {

    private static final Path TEST_DIR = Paths.get("../20-tsc-cm-io/src/test/resources/util/serzdetector");

    @Test
    void testRootCommandInvocationWithoutSubcommand() {
        ToolRunner toolRunner = new ToolRunner();
        CommandLine cmd = new CommandLine(toolRunner);

        int exitCode = cmd.execute();

        assertEquals(0, exitCode);
    }

    @Test
    void testRootCommandHelp() {
        ToolRunner toolRunner = new ToolRunner();
        CommandLine cmd = new CommandLine(toolRunner);

        int exitCode = cmd.execute("--help");

        assertEquals(0, exitCode);
    }

    @Test
    void testSubcommandExecution() {
        ToolRunner toolRunner = new ToolRunner();
        CommandLine cmd = new CommandLine(toolRunner);

        int exitCode = cmd.execute("serz-tag-detector", "-f", TEST_DIR.toString(), "-k");

        assertEquals(0, exitCode);
    }

    @Test
    void testSubcommandHelp() {
        ToolRunner toolRunner = new ToolRunner();
        CommandLine cmd = new CommandLine(toolRunner);

        int exitCode = cmd.execute("serz-tag-detector", "--help");

        assertEquals(0, exitCode);
    }

    @Test
    void testInvalidSubcommand() {
        ToolRunner toolRunner = new ToolRunner();
        CommandLine cmd = new CommandLine(toolRunner);

        int exitCode = cmd.execute("non-existent-tool");

        assertNotEquals(0, exitCode);
    }
}
