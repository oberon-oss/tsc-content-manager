package eu.oberonoss.tsc.cm.base.cfg;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import oshi.PlatformEnum;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TSCInstallationDetectorTest {

    @Nested
    class ConstructorAndClassTests {

        @Test
        void constructorIsPrivate() throws NoSuchMethodException {
            Constructor<TSCInstallationDetector> constructor = TSCInstallationDetector.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }

        @Test
        void privateConstructorCanBeInvokedReflectively() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            Constructor<TSCInstallationDetector> constructor = TSCInstallationDetector.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            TSCInstallationDetector instance = constructor.newInstance();
            assertNotNull(instance);
        }
    }

    @Nested
    class ConstantsTests {

        @Test
        void constantsAreProperlyDefined() {
            assertEquals("RailWorks64.exe", TSCInstallationDetector.RAILWORKS_64_EXE);
            assertEquals("RailWorks.exe", TSCInstallationDetector.RAILWORKS_EXE);
            assertEquals("RailWorks", TSCInstallationDetector.RAILWORKS_DIR_NAME);
            assertEquals("Steam", TSCInstallationDetector.STEAM_DIR_NAME);
            assertEquals("steamapps", TSCInstallationDetector.STEAMAPPS_DIR_NAME);
            assertEquals("common", TSCInstallationDetector.COMMON_DIR_NAME);
            assertEquals(".steam", TSCInstallationDetector.DOT_STEAM_DIR_NAME);
        }
    }

    @Nested
    class PlatformCandidatesResolution {

        @Test
        void candidatePathsForWindowsUsesEnvironmentVariables() {
            Map<String, String> env = new HashMap<>();
            env.put("ProgramFiles(x86)", "D:\\Program Files (x86)");
            env.put("ProgramFiles", "D:\\Program Files");
            env.put("LOCALAPPDATA", "D:\\Users\\Test\\AppData\\Local");

            List<Path> candidates = TSCInstallationDetector.getCandidateInstallationPaths(PlatformEnum.WINDOWS, "D:\\Users\\Test", env);
            assertNotNull(candidates);
            assertFalse(candidates.isEmpty());
            assertTrue(candidates.contains(Paths.get("D:\\Program Files (x86)", "Steam", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get("D:\\Program Files", "Steam", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get("D:\\Users\\Test\\AppData\\Local", "Programs", "Steam", "steamapps", "common", "RailWorks")));
        }

        @Test
        void candidatePathsForMacOSUsesUserHome() {
            String userHome = "/Users/testuser";
            List<Path> candidates = TSCInstallationDetector.getCandidateInstallationPaths(PlatformEnum.MACOS, userHome, Collections.emptyMap());
            assertNotNull(candidates);
            assertTrue(candidates.contains(Paths.get(userHome, "Library", "Application Support", "Steam", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(userHome, "Library", "Application Support", "Steam", "SteamApps", "common", "RailWorks")));
        }

        @Test
        void candidatePathsForLinuxUsesUserHome() {
            String userHome = "/home/testuser";
            List<Path> candidates = TSCInstallationDetector.getCandidateInstallationPaths(PlatformEnum.LINUX, userHome, Collections.emptyMap());
            assertNotNull(candidates);
            assertTrue(candidates.contains(Paths.get(userHome, ".local", "share", "Steam", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(userHome, ".steam", "steam", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(userHome, ".steam", "root", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(userHome, ".steam", "debian-installation", "steamapps", "common", "RailWorks")));
        }

        @ParameterizedTest
        @EnumSource(PlatformEnum.class)
        void resolveDefaultInstallationDirectoryReturnsNonNullForAnyPlatform(PlatformEnum platform) {
            Path path = TSCInstallationDetector.resolveDefaultInstallationDirectory(platform);
            assertNotNull(path);
        }

        @Test
        void resolveDefaultInstallationDirectoryDetectsExistingCandidateDirectory(@TempDir Path tempDir) throws IOException {
            Path fakeSteamRailworks = tempDir.resolve("Steam/steamapps/common/RailWorks");
            Files.createDirectories(fakeSteamRailworks);
            Files.createFile(fakeSteamRailworks.resolve("RailWorks64.exe"));

            Map<String, String> env = Map.of("ProgramFiles(x86)", tempDir.toString());
            Path resolved = TSCInstallationDetector.resolveDefaultInstallationDirectory(PlatformEnum.WINDOWS, tempDir.toString(), env);
            assertEquals(fakeSteamRailworks, resolved);
        }
    }

    @Nested
    class SystemPropertiesAndEnvironmentOverrides {

        @Test
        void systemPropertyTscInstallationDirectoryOverridesDetection(@TempDir Path tempDir) {
            String original = System.getProperty("tsc.installation.directory");
            try {
                System.setProperty("tsc.installation.directory", tempDir.toString());
                Path resolved = TSCInstallationDetector.resolveDefaultInstallationDirectory();
                assertEquals(tempDir.toAbsolutePath().normalize(), resolved);
            } finally {
                if (original != null) {
                    System.setProperty("tsc.installation.directory", original);
                } else {
                    System.clearProperty("tsc.installation.directory");
                }
            }
        }

        @Test
        void systemPropertyTscInstallDirOverridesDetection(@TempDir Path tempDir) {
            String original = System.getProperty("tsc.install.dir");
            try {
                System.setProperty("tsc.install.dir", tempDir.toString());
                Path resolved = TSCInstallationDetector.resolveDefaultInstallationDirectory();
                assertEquals(tempDir.toAbsolutePath().normalize(), resolved);
            } finally {
                if (original != null) {
                    System.setProperty("tsc.install.dir", original);
                } else {
                    System.clearProperty("tsc.install.dir");
                }
            }
        }

        @Test
        void systemPropertyTscHomeOverridesDetection(@TempDir Path tempDir) {
            String original = System.getProperty("tsc.home");
            try {
                System.setProperty("tsc.home", tempDir.toString());
                Path resolved = TSCInstallationDetector.resolveDefaultInstallationDirectory();
                assertEquals(tempDir.toAbsolutePath().normalize(), resolved);
            } finally {
                if (original != null) {
                    System.setProperty("tsc.home", original);
                } else {
                    System.clearProperty("tsc.home");
                }
            }
        }
    }

    @Nested
    class DirectoryValidationAndNormalization {

        @Test
        void isValidInstallationDirectoryReturnsFalseForNullOrNonExistent() {
            assertFalse(TSCInstallationDetector.isValidInstallationDirectory(null));
            assertFalse(TSCInstallationDetector.isValidInstallationDirectory(Paths.get("/non/existent/path/for/tsc")));
        }

        @Test
        void isValidInstallationDirectoryReturnsTrueWhenExecutableExists(@TempDir Path tempDir) throws IOException {
            assertFalse(TSCInstallationDetector.isValidInstallationDirectory(tempDir));

            Path exe64 = tempDir.resolve("RailWorks64.exe");
            Files.createFile(exe64);
            assertTrue(TSCInstallationDetector.isValidInstallationDirectory(tempDir));
            Files.delete(exe64);

            Path exe32 = tempDir.resolve("RailWorks.exe");
            Files.createFile(exe32);
            assertTrue(TSCInstallationDetector.isValidInstallationDirectory(tempDir));
            Files.delete(exe32);

            Path assetsDir = tempDir.resolve("Assets");
            Files.createDirectory(assetsDir);
            assertTrue(TSCInstallationDetector.isValidInstallationDirectory(tempDir));
        }

        @Test
        void isInstallationExecutableRecognizesExpectedFileNames() {
            assertFalse(TSCInstallationDetector.isInstallationExecutable(null));
            assertFalse(TSCInstallationDetector.isInstallationExecutable(Paths.get("somethingElse.exe")));
            assertTrue(TSCInstallationDetector.isInstallationExecutable(Paths.get("RailWorks64.exe")));
            assertTrue(TSCInstallationDetector.isInstallationExecutable(Paths.get("RailWorks.exe")));
            assertTrue(TSCInstallationDetector.isInstallationExecutable(Paths.get("railworks64.exe")));
            assertTrue(TSCInstallationDetector.isInstallationExecutable(Paths.get("railworks.exe")));
            assertTrue(TSCInstallationDetector.isInstallationExecutable(Paths.get("C:/Games/RailWorks/RailWorks64.exe")));
        }

        @Test
        void normalizeInstallationPathReturnsNullForNull() {
            assertNull(TSCInstallationDetector.normalizeInstallationPath(null));
        }

        @Test
        void normalizeInstallationPathExtractsParentForExecutableFile(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("RailWorks64.exe");
            Files.createFile(file);

            Path normalized = TSCInstallationDetector.normalizeInstallationPath(file);
            assertEquals(tempDir.toAbsolutePath().normalize(), normalized);
        }
    }
}
