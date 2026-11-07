package eu.oberon.oss.tsc.cm.cli.tools;

import eu.oberon.oss.tsc.cm.cli.ToolNameConverter;
import eu.oberonoss.tsc.cm.gui.TSCApplication;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command line tool to launch the TSC Application GUI in a separate process.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Command(
        name = "tsc-application",
        description = "Launches the TSC Application GUI in a separate process",
        mixinStandardHelpOptions = true
)
@Slf4j(topic = "tsc-application")
public class TSCApplicationCLI implements Callable<Integer> {

    @Option(
            names = {"--jvm-args", "-j"},
            description = "Optional additional JVM arguments to pass to the launched application process",
            split = ","
    )
    List<String> jvmArgs;

    @Option(
            names = {"--wait", "-w"},
            description = "Wait for the application process to exit before returning",
            defaultValue = "false"
    )
    boolean wait;

    /**
     * Default constructor for TSCApplicationCLI.
     *
     * @since 1.0.0
     */
    public TSCApplicationCLI() {
        // Keep Javadoc happy
    }

    /**
     * Entry point for running the TSC application CLI tool directly.
     *
     * @param args Command line arguments.
     *
     * @since 1.0.0
     */
    static void main(String[] args) {
        CommandLine cmd = new CommandLine(new TSCApplicationCLI());
        cmd.setExecutionStrategy(parseResult -> {
            ToolNameConverter.setToolName(TSCApplicationCLI.class);
            return new CommandLine.RunLast().execute(parseResult);
        });
        System.exit(cmd.execute(args));
    }

    /**
     * Executes the process to launch the TSC Application in a separate process.
     *
     * @return The exit code (0 for success, non-zero for failure).
     *
     * @since 1.0.0
     */
    @Override
    public Integer call() {
        ToolNameConverter.setToolName(getClass());
        LOGGER.info("Starting TSC Application in a separate process...");
        try {
            Process process = launchApplication();
            LOGGER.info("TSC Application launched successfully (PID: {}).", process.pid());
            if (wait) {
                return process.waitFor();
            }
            return 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Process execution was interrupted: {}", e.getMessage(), e);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Failed to launch TSC Application: {}", e.getMessage(), e);
            return 1;
        }
    }

    /**
     * Launches the TSC application process.
     *
     * @return The started Process.
     *
     * @throws IOException If process creation fails.
     * @since 1.0.0
     */
    Process launchApplication() throws IOException {
        ProcessBuilder processBuilder = createProcessBuilder();
        return processBuilder.start();
    }

    /**
     * Creates a {@link ProcessBuilder} configured to launch the TSC application.
     *
     * @return The configured ProcessBuilder.
     *
     * @since 1.0.0
     */
    ProcessBuilder createProcessBuilder() {
        List<String> command = buildCommand();
        LOGGER.debug("Executing command: {}", command);
        return new ProcessBuilder(command);
    }

    /**
     * Builds the command line list for launching the JVM process.
     *
     * @return List of command line arguments.
     *
     * @since 1.0.0
     */
    List<String> buildCommand() {
        List<String> command = new ArrayList<>();
        command.add(getJavaExecutable());

        String appHome = System.getProperty("app.home");
        if (appHome != null && !appHome.isBlank()) {
            command.add("-Dapp.home=" + appHome);
        }

        if (jvmArgs != null && !jvmArgs.isEmpty()) {
            command.addAll(jvmArgs);
        }

        command.add("-cp");
        command.add(getClasspath());
        command.add(TSCApplication.class.getName());

        return command;
    }

    /**
     * Resolves the Java executable path.
     *
     * @return Path to java binary.
     *
     * @since 1.0.0
     */
    String getJavaExecutable() {
        return ProcessHandle.current()
                .info()
                .command()
                .orElseGet(() -> Paths.get(System.getProperty("java.home"), "bin", "java").toString());
    }

    /**
     * Resolves the current classpath.
     *
     * @return Classpath string.
     *
     * @since 1.0.0
     */
    String getClasspath() {
        return System.getProperty("java.class.path");
    }
}
