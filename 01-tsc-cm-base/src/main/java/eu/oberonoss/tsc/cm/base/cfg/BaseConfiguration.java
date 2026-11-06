package eu.oberonoss.tsc.cm.base.cfg;

import oshi.PlatformEnum;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Base configuration for Train Simulator Classic (TSC) Content Manager.
 * <p>
 * Manages the TSC installation directory configuration and provides automatic detection across Windows, Linux, and macOS platforms.
 * </p>
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class BaseConfiguration {


    private static final BaseConfiguration INSTANCE = new BaseConfiguration();

    private Path tscInstallationDirectory;

    /**
     * Creates a new default BaseConfiguration.
     *
     * @since 1.0.0
     */
    public BaseConfiguration() {
        this.tscInstallationDirectory = TSCInstallationDetector.resolveDefaultInstallationDirectory().getRoot();
    }

    /**
     * Creates a new BaseConfiguration with the specified installation directory.
     *
     * @param tscInstallationDirectory The installation directory or executable path.
     *
     * @since 1.0.0
     */
    public BaseConfiguration(Path tscInstallationDirectory) {
        setTSCInstallationDirectory(tscInstallationDirectory);
    }

    /**
     * Creates a new BaseConfiguration with the specified installation directory string.
     *
     * @param tscInstallationDirectory The installation directory or executable path as a string.
     *
     * @since 1.0.0
     */
    public BaseConfiguration(String tscInstallationDirectory) {
        setTSCInstallationDirectory(tscInstallationDirectory);
    }

    /**
     * Retrieves the singleton instance of BaseConfiguration.
     *
     * @return The singleton instance of BaseConfiguration.
     *
     * @since 1.0.0
     */
    public static BaseConfiguration getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves the configured Train Simulator Classic installation directory.
     * <p>
     * If no installation directory has been explicitly set, attempts to auto-detect or resolve the default installation directory for the current platform.
     * </p>
     *
     * @return The Path to the TSC installation directory.
     *
     * @since 1.0.0
     */
    public Path getTSCInstallationDirectory() {
        if (tscInstallationDirectory != null) {
            return tscInstallationDirectory;
        }
        return resolveDefaultInstallationDirectory();
    }

    /**
     * Sets the Train Simulator Classic installation directory.
     * <p>
     * If the specified path is a regular file (e.g., RailWorks.exe or RailWorks64.exe), its parent directory is set as the installation directory.
     * </p>
     *
     * @param tscInstallationDirectory The path to the TSC installation directory or executable.
     *
     * @since 1.0.0
     */
    public void setTSCInstallationDirectory(Path tscInstallationDirectory) {
        this.tscInstallationDirectory = normalizeInstallationPath(tscInstallationDirectory);
    }

    /**
     * Sets the Train Simulator Classic installation directory from a string path.
     *
     * @param tscInstallationDirectory The path string to the TSC installation directory or executable.
     *
     * @since 1.0.0
     */
    public void setTSCInstallationDirectory(String tscInstallationDirectory) {
        if (tscInstallationDirectory == null || tscInstallationDirectory.isBlank()) {
            this.tscInstallationDirectory = null;
        } else {
            setTSCInstallationDirectory(Paths.get(tscInstallationDirectory));
        }
    }

    /**
     * Sets the Train Simulator Classic installation directory from a {@link File}.
     *
     * @param tscInstallationDirectory The File representing the TSC installation directory or executable.
     *
     * @since 1.0.0
     */
    public void setTSCInstallationDirectory(File tscInstallationDirectory) {
        if (tscInstallationDirectory == null) {
            this.tscInstallationDirectory = null;
        } else {
            setTSCInstallationDirectory(tscInstallationDirectory.toPath());
        }
    }

    /**
     * Resolves the default TSC installation directory for the current running environment.
     * <p>
     * Checks system properties ({@code tsc.installation.directory}, {@code tsc.install.dir}, {@code tsc.home}) and environment variables
     * ({@code TSC_INSTALLATION_DIRECTORY}, {@code TSC_INSTALL_DIR}, {@code TSC_HOME}), falling back to standard OS-specific Steam directories on Windows,
     * Linux, and macOS.
     * </p>
     *
     * @return The resolved default installation directory path.
     *
     * @since 1.0.0
     */
    public static Path resolveDefaultInstallationDirectory() {
        return TSCInstallationDetector.resolveDefaultInstallationDirectory();
    }

    /**
     * Resolves the default TSC installation directory for a given platform.
     *
     * @param platform The target platform.
     *
     * @return The resolved default installation directory path for the platform.
     *
     * @since 1.0.0
     */
    public static Path resolveDefaultInstallationDirectory(PlatformEnum platform) {
        return TSCInstallationDetector.resolveDefaultInstallationDirectory(platform);
    }

    /**
     * Resolves the default TSC installation directory for a given platform, user home directory, and environment.
     *
     * @param platform The target platform.
     * @param userHome The user home directory path.
     * @param env      The environment variables map.
     *
     * @return The resolved default installation directory path.
     *
     * @since 1.0.0
     */
    public static Path resolveDefaultInstallationDirectory(PlatformEnum platform, String userHome, Map<String, String> env) {
        return TSCInstallationDetector.resolveDefaultInstallationDirectory(platform, userHome, env);
    }

    /**
     * Returns candidate installation directories for a given platform.
     *
     * @param platform The target platform.
     * @param userHome The user home directory path.
     * @param env      The environment variables map.
     *
     * @return A list of candidate paths in order of preference.
     *
     * @since 1.0.0
     */
    public static List<Path> getCandidateInstallationPaths(PlatformEnum platform, String userHome, Map<String, String> env) {
        return TSCInstallationDetector.getCandidateInstallationPaths(platform, userHome, env);
    }

    /**
     * Checks whether the specified path represents a valid Train Simulator Classic installation directory.
     *
     * @param path The path to check.
     *
     * @return {@code true} if the path is an existing directory containing known TSC files, {@code false} otherwise.
     *
     * @since 1.0.0
     */
    public static boolean isValidInstallationDirectory(Path path) {
        return TSCInstallationDetector.isValidInstallationDirectory(path);
    }

    /**
     * Checks whether the specified path represents a Train Simulator Classic executable.
     *
     * @param path The path to check.
     *
     * @return {@code true} if the path points to RailWorks.exe or RailWorks64.exe, {@code false} otherwise.
     *
     * @since 1.0.0
     */
    public static boolean isInstallationExecutable(Path path) {
        return TSCInstallationDetector.isInstallationExecutable(path);
    }

    /**
     * Normalizes a given path to its installation directory path. If the path is a regular file or an executable, its parent directory is returned.
     *
     * @param path The path to normalize.
     *
     * @return The normalized installation directory path, or {@code null} if input is null.
     *
     * @since 1.0.0
     */
    public static Path normalizeInstallationPath(Path path) {
        return TSCInstallationDetector.normalizeInstallationPath(path);
    }
}
