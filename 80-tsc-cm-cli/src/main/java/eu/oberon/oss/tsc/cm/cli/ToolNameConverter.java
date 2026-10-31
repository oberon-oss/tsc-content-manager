package eu.oberon.oss.tsc.cm.cli;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import picocli.CommandLine.Command;

/**
 * Logback converter that dynamically supplies the name of the currently executing CLI tool.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class ToolNameConverter extends ClassicConverter {

    private static volatile String currentToolName;

    /**
     * Default constructor for ToolNameConverter.
     *
     * @since 1.0.0
     */
    public ToolNameConverter() {
        // Keep Javadoc happy
    }

    /**
     * Sets the active tool name.
     *
     * @param toolName Name of the tool.
     *
     * @since 1.0.0
     */
    public static void setToolName(String toolName) {
        currentToolName = toolName;
    }

    /**
     * Sets the active tool name from a class, resolving {@link Command#name()} if present.
     *
     * @param toolClass Tool class.
     *
     * @since 1.0.0
     */
    public static void setToolName(Class<?> toolClass) {
        if (toolClass == null) {
            currentToolName = null;
            return;
        }
        Command command = toolClass.getAnnotation(Command.class);
        if (command != null && !command.name().isEmpty() && !"<main class>".equals(command.name())) {
            currentToolName = command.name();
        } else {
            currentToolName = toolClass.getSimpleName();
        }
    }

    /**
     * Returns the current tool name.
     *
     * @return Current tool name or null if not set.
     *
     * @since 1.0.0
     */
    public static String getToolName() {
        return currentToolName;
    }

    /**
     * Resets the current tool name.
     *
     * @since 1.0.0
     */
    public static void reset() {
        currentToolName = null;
    }

    /**
     * Formats the logging event by returning the current tool name or falling back to logger name.
     *
     * @param event The logging event.
     *
     * @return The tool name or fallback logger name.
     *
     * @since 1.0.0
     */
    @Override
    public String convert(ILoggingEvent event) {
        if (currentToolName != null && !currentToolName.isEmpty()) {
            return currentToolName;
        }
        if (event == null || event.getLoggerName() == null) {
            return "tsc-tool";
        }
        String loggerName = event.getLoggerName();
        int lastDot = loggerName.lastIndexOf('.');
        return lastDot >= 0 ? loggerName.substring(lastDot + 1) : loggerName;
    }
}
