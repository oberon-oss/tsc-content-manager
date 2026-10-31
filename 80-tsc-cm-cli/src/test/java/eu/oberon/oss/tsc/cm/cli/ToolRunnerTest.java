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
        CommandLine cmd = ToolRunner.createCommandLine();

        int exitCode = cmd.execute();

        assertEquals(0, exitCode);
        assertEquals("tsc-tool-runner", ToolNameConverter.getToolName());
    }

    @Test
    void testRootCommandHelp() {
        CommandLine cmd = ToolRunner.createCommandLine();

        int exitCode = cmd.execute("--help");

        assertEquals(0, exitCode);
    }

    @Test
    void testSubcommandExecution() {
        CommandLine cmd = ToolRunner.createCommandLine();

        int exitCode = cmd.execute("serz-tag-detector", "-f", TEST_DIR.toString(), "-k");

        assertEquals(0, exitCode);
        assertEquals("serz-tag-detector", ToolNameConverter.getToolName());
    }

    @Test
    void testSubcommandHelp() {
        CommandLine cmd = ToolRunner.createCommandLine();

        int exitCode = cmd.execute("serz-tag-detector", "--help");

        assertEquals(0, exitCode);
    }

    @Test
    void testInvalidSubcommand() {
        CommandLine cmd = ToolRunner.createCommandLine();

        int exitCode = cmd.execute("non-existent-tool");

        assertNotEquals(0, exitCode);
    }

    @CommandLine.Command(name = "sample-tool")
    static class SampleTool implements java.util.concurrent.Callable<Integer> {
        @Override
        public Integer call() {
            return 0;
        }
    }

    @Test
    void testDynamicSubcommandExecutionSetsToolName() {
        CommandLine cmd = ToolRunner.createCommandLine();
        cmd.addSubcommand("sample-tool", new SampleTool());

        int exitCode = cmd.execute("sample-tool");

        assertEquals(0, exitCode);
        assertEquals("sample-tool", ToolNameConverter.getToolName());
    }
}
