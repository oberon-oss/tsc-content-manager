package eu.oberon.oss.tsc.cm.cli;

import eu.oberon.oss.tsc.cm.cli.tools.SERZTagDetectorCLI;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

/**
 * Unified entry point for TSC Content Manager command line tools.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Command(
        name = "tsc-tool-runner",
        description = "Unified entry point for TSC Content Manager command line tools",
        mixinStandardHelpOptions = true,
        subcommands = {
                SERZTagDetectorCLI.class
        }
)
public class ToolRunner implements Callable<Integer> {

    /**
     * Default constructor for ToolRunner.
     *
     * @since 1.0.0
     */
    public ToolRunner() {
        // Keep Javadoc happy
    }

    /**
     * Executes the default command when no subcommand is specified.
     *
     * @return The exit code of the command.
     * @since 1.0.0
     */
    @Override
    @SuppressWarnings("java:S106")
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    /**
     * Configures the active tool name based on the command or subcommand being executed.
     *
     * @param parseResult The parse result.
     *
     * @return Execution result code.
     *
     * @since 1.0.0
     */
    static int executionStrategy(CommandLine.ParseResult parseResult) {
        CommandLine.ParseResult last = parseResult;
        while (last.hasSubcommand()) {
            last = last.subcommand();
        }
        String toolName = last.commandSpec().name();
        ToolNameConverter.setToolName(toolName);
        return new CommandLine.RunLast().execute(parseResult);
    }

    /**
     * Creates a configured {@link CommandLine} instance for {@link ToolRunner}.
     *
     * @return Configured {@link CommandLine} instance.
     *
     * @since 1.0.0
     */
    public static CommandLine createCommandLine() {
        CommandLine cmd = new CommandLine(new ToolRunner());
        cmd.setExecutionStrategy(ToolRunner::executionStrategy);
        return cmd;
    }

    /**
     * Entry point for running CLI tools.
     *
     * @param args Command line arguments.
     * @since 1.0.0
     */
    static void main(String[] args) {
        int exitCode = createCommandLine().execute(args);
        System.exit(exitCode);
    }

}
