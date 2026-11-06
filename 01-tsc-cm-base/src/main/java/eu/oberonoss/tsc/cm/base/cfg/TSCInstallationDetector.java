package eu.oberonoss.tsc.cm.base.cfg;

import oshi.PlatformEnum;
import oshi.SystemInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Detects and resolves the Train Simulator Classic (TSC) installation directory across platforms.
 * <p>
 * Provides automatic detection on Windows, Linux, and macOS platforms, resolution via system properties and environment variables, path normalization, and
 * validation.
 * </p>
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class TSCInstallationDetector {

    /**
     * Standard RailWorks executable name for 64-bit systems.
     *
     * @since 1.0.0
     */
    public static final String RAILWORKS_64_EXE = "RailWorks64.exe";

    /**
     * Standard RailWorks executable name for 32-bit systems.
     *
     * @since 1.0.0
     */
    public static final String RAILWORKS_EXE = "RailWorks.exe";

    /**
     * Relative path to Steamapps common RailWorks directory.
     *
     * @since 1.0.0
     */
    public static final String RAILWORKS_DIR_NAME = "RailWorks";

    /**
     * Standard Steam directory name.
     *
     * @since 1.0.0
     */
    public static final String STEAM_DIR_NAME = "Steam";

    /**
     * Standard Steamapps directory name.
     *
     * @since 1.0.0
     */
    public static final String STEAMAPPS_DIR_NAME = "steamapps";

    /**
     * Standard Steam common directory name.
     *
     * @since 1.0.0
     */
    public static final String COMMON_DIR_NAME = "common";

    /**
     * Standard Linux dot-steam directory name.
     *
     * @since 1.0.0
     */
    public static final String DOT_STEAM_DIR_NAME = ".steam";

    private TSCInstallationDetector() {
        // Utility class
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
        String property = System.getProperty("tsc.installation.directory");
        if (property != null && !property.isBlank()) {
            return normalizeInstallationPath(Paths.get(property));
        }
        String propertyAlt = System.getProperty("tsc.install.dir");
        if (propertyAlt != null && !propertyAlt.isBlank()) {
            return normalizeInstallationPath(Paths.get(propertyAlt));
        }
        String propertyHome = System.getProperty("tsc.home");
        if (propertyHome != null && !propertyHome.isBlank()) {
            return normalizeInstallationPath(Paths.get(propertyHome));
        }

        String env = System.getenv("TSC_INSTALLATION_DIRECTORY");
        if (env != null && !env.isBlank()) {
            return normalizeInstallationPath(Paths.get(env));
        }
        String envAlt = System.getenv("TSC_INSTALL_DIR");
        if (envAlt != null && !envAlt.isBlank()) {
            return normalizeInstallationPath(Paths.get(envAlt));
        }
        String envHome = System.getenv("TSC_HOME");
        if (envHome != null && !envHome.isBlank()) {
            return normalizeInstallationPath(Paths.get(envHome));
        }

        PlatformEnum platform = SystemInfo.getCurrentPlatform();
        return resolveDefaultInstallationDirectory(platform);
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
        String userHome = System.getProperty("user.home", "");
        return resolveDefaultInstallationDirectory(platform, userHome, System.getenv());
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
        List<Path> candidates = getCandidateInstallationPaths(platform, userHome, env);
        for (Path candidate : candidates) {
            if (isValidInstallationDirectory(candidate)) {
                return candidate;
            }
        }
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return candidates.isEmpty() ? Paths.get(userHome) : candidates.getFirst();
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
        List<Path> candidates = new ArrayList<>();
        switch (platform) {
            case PlatformEnum.WINDOWS -> attemptToResolveForWindowsPlatform(env, candidates);
            case PlatformEnum.MACOS -> attemptToResolveForMacOSPlatform(userHome, candidates);
            default -> attemptToResolveForLinuxPlatform(userHome, candidates);
        }
        return candidates;
    }

    private static void attemptToResolveForWindowsPlatform(Map<String, String> env, List<Path> candidates) {
        String programFilesX86 = env != null ? env.get("ProgramFiles(x86)") : null;
        if (programFilesX86 != null && !programFilesX86.isBlank()) {
            candidates.add(Paths.get(programFilesX86, STEAM_DIR_NAME, STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        }
        String programFiles = env != null ? env.get("ProgramFiles") : null;
        if (programFiles != null && !programFiles.isBlank()) {
            candidates.add(Paths.get(programFiles, STEAM_DIR_NAME, STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        }
        candidates.add(Paths.get("C:", "Program Files (x86)", STEAM_DIR_NAME, STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        candidates.add(Paths.get("C:", "Program Files", STEAM_DIR_NAME, STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        String localAppData = env != null ? env.get("LOCALAPPDATA") : null;
        if (localAppData != null && !localAppData.isBlank()) {
            candidates.add(Paths.get(localAppData, "Programs", STEAM_DIR_NAME, STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        }
    }

    private static void attemptToResolveForMacOSPlatform(String userHome, List<Path> candidates) {
        candidates.add(Paths.get(userHome, "Library", "Application Support", STEAM_DIR_NAME, STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        candidates.add(Paths.get(userHome, "Library", "Application Support", STEAM_DIR_NAME, "SteamApps", COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
    }

    private static void attemptToResolveForLinuxPlatform(String userHome, List<Path> candidates) {
        // Linux and other Unix-like systems
        candidates.add(Paths.get(userHome, ".local", "share", STEAM_DIR_NAME, STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        candidates.add(Paths.get(userHome, DOT_STEAM_DIR_NAME, "steam", STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        candidates.add(Paths.get(userHome, DOT_STEAM_DIR_NAME, "root", STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        candidates.add(Paths.get(userHome, DOT_STEAM_DIR_NAME, "debian-installation", STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        candidates.add(Paths.get(userHome, ".var", "app", "com.valvesoftware.Steam", "data", STEAM_DIR_NAME, STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
        candidates.add(Paths.get(userHome, "snap", "steam", COMMON_DIR_NAME, ".local", "share", STEAM_DIR_NAME, STEAMAPPS_DIR_NAME, COMMON_DIR_NAME, RAILWORKS_DIR_NAME));
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
        if (path == null) {
            return false;
        }
        if (!Files.isDirectory(path)) {
            return false;
        }
        return Files.exists(path.resolve(RAILWORKS_64_EXE))
                || Files.exists(path.resolve(RAILWORKS_EXE))
                || Files.exists(path.resolve("railworks.exe"))
                || Files.exists(path.resolve("Assets"))
                || Files.exists(path.resolve("Content"));
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
        if (path == null) {
            return false;
        }
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }
        String name = fileName.toString();
        return RAILWORKS_64_EXE.equalsIgnoreCase(name) || RAILWORKS_EXE.equalsIgnoreCase(name);
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
        if (path == null) {
            return null;
        }
        Path absolute = path.toAbsolutePath().normalize();
        if (Files.isRegularFile(absolute) || isInstallationExecutable(absolute)) {
            Path parent = absolute.getParent();
            return parent != null ? parent : absolute;
        }
        return absolute;
    }
}
