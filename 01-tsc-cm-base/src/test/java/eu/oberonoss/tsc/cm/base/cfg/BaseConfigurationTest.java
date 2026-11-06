package eu.oberonoss.tsc.cm.base.cfg;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import oshi.PlatformEnum;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BaseConfigurationTest {

    @Nested
    class ConstructionAndSetters {

        @Test
        void defaultConstructorInitializesNullFieldAndResolvesDefault() {
            BaseConfiguration config = new BaseConfiguration();
            Path path = config.getTSCInstallationDirectory();
            assertNotNull(path);
        }

        @Test
        void pathConstructorSetsDirectory(@TempDir Path tempDir) {
            BaseConfiguration config = new BaseConfiguration(tempDir);
            assertEquals(tempDir.toAbsolutePath().normalize(), config.getTSCInstallationDirectory());
        }

        @Test
        void stringConstructorSetsDirectory(@TempDir Path tempDir) {
            BaseConfiguration config = new BaseConfiguration(tempDir.toString());
            assertEquals(tempDir.toAbsolutePath().normalize(), config.getTSCInstallationDirectory());
        }

        @Test
        void fileSetterSetsDirectory(@TempDir Path tempDir) {
            BaseConfiguration config = new BaseConfiguration();
            config.setTSCInstallationDirectory(tempDir.toFile());
            assertEquals(tempDir.toAbsolutePath().normalize(), config.getTSCInstallationDirectory());

            config.setTSCInstallationDirectory((File) null);
            assertNotNull(config.getTSCInstallationDirectory());
        }

        @Test
        void stringSetterSetsDirectory(@TempDir Path tempDir) {
            BaseConfiguration config = new BaseConfiguration();
            config.setTSCInstallationDirectory(tempDir.toString());
            assertEquals(tempDir.toAbsolutePath().normalize(), config.getTSCInstallationDirectory());

            config.setTSCInstallationDirectory((String) null);
            assertNotNull(config.getTSCInstallationDirectory());

            config.setTSCInstallationDirectory("   ");
            assertNotNull(config.getTSCInstallationDirectory());
        }

        @Test
        void pathSetterSetsDirectory(@TempDir Path tempDir) {
            BaseConfiguration config = new BaseConfiguration();
            config.setTSCInstallationDirectory(tempDir);
            assertEquals(tempDir.toAbsolutePath().normalize(), config.getTSCInstallationDirectory());

            config.setTSCInstallationDirectory((Path) null);
            assertNotNull(config.getTSCInstallationDirectory());
        }

        @Test
        void settingExecutableFileSetsParentDirectory(@TempDir Path tempDir) throws IOException {
            Path exeFile = tempDir.resolve("RailWorks64.exe");
            Files.createFile(exeFile);

            BaseConfiguration config = new BaseConfiguration(exeFile);
            assertEquals(tempDir.toAbsolutePath().normalize(), config.getTSCInstallationDirectory());

            Path exe32 = tempDir.resolve("RailWorks.exe");
            Files.createFile(exe32);
            config.setTSCInstallationDirectory(exe32);
            assertEquals(tempDir.toAbsolutePath().normalize(), config.getTSCInstallationDirectory());
        }

        @Test
        void getInstanceReturnsNonNullSingleton() {
            BaseConfiguration instance = BaseConfiguration.getInstance();
            assertNotNull(instance);
            assertSame(instance, BaseConfiguration.getInstance());
        }
    }

    @Nested
    class PlatformDetection {

        @Test
        void windowsCandidatePathsWithEnvironmentVariables() {
            Map<String, String> env = new HashMap<>();
            env.put("ProgramFiles(x86)", "D:\\Games\\ProgramFilesX86");
            env.put("ProgramFiles", "D:\\Games\\ProgramFiles");
            env.put("LOCALAPPDATA", "D:\\Games\\LocalAppData");

            List<Path> candidates = BaseConfiguration.getCandidateInstallationPaths(PlatformEnum.WINDOWS, "C:\\Users\\Test", env);
            assertFalse(candidates.isEmpty());

            boolean hasX86Env = candidates.stream().anyMatch(p -> p.toString().contains("ProgramFilesX86"));
            boolean has64Env = candidates.stream().anyMatch(p -> p.toString().contains("ProgramFiles"));
            boolean hasDefaultC = candidates.stream().anyMatch(p -> p.toString().contains("C:"));

            assertTrue(hasX86Env);
            assertTrue(has64Env);
            assertTrue(hasDefaultC);
        }

        @Test
        void linuxCandidatePathsContainStandardLocations() {
            String home = "/home/testuser";
            List<Path> candidates = BaseConfiguration.getCandidateInstallationPaths(PlatformEnum.LINUX, home, Collections.emptyMap());

            assertFalse(candidates.isEmpty());
            assertTrue(candidates.contains(Paths.get(home, ".local", "share", "Steam", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(home, ".steam", "steam", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(home, ".steam", "root", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(home, ".steam", "debian-installation", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(home, ".var", "app", "com.valvesoftware.Steam", "data", "Steam", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(home, "snap", "steam", "common", ".local", "share", "Steam", "steamapps", "common", "RailWorks")));
        }

        @Test
        void macOsCandidatePathsContainStandardLocations() {
            String home = "/Users/testuser";
            List<Path> candidates = BaseConfiguration.getCandidateInstallationPaths(PlatformEnum.MACOS, home, Collections.emptyMap());

            assertFalse(candidates.isEmpty());
            assertTrue(candidates.contains(Paths.get(home, "Library", "Application Support", "Steam", "steamapps", "common", "RailWorks")));
            assertTrue(candidates.contains(Paths.get(home, "Library", "Application Support", "Steam", "SteamApps", "common", "RailWorks")));
        }

        @ParameterizedTest
        @EnumSource(PlatformEnum.class)
        void resolveDefaultInstallationDirectoryForPlatformReturnsNonNull(PlatformEnum platform) {
            Path resolved = BaseConfiguration.resolveDefaultInstallationDirectory(platform);
            assertNotNull(resolved);
        }

        @Test
        void resolveDetectsExistingInstallationDirectory(@TempDir Path tempDir) throws IOException {
            Path fakeRailworks = tempDir.resolve("RailWorks");
            Files.createDirectories(fakeRailworks);
            Files.createFile(fakeRailworks.resolve("RailWorks64.exe"));

            String fakeHome = tempDir.toString();
            // Create linux structure
            Path linuxSteamCommon = tempDir.resolve(".local/share/Steam/steamapps/common/RailWorks");
            Files.createDirectories(linuxSteamCommon);
            Files.createFile(linuxSteamCommon.resolve("RailWorks64.exe"));

            Path resolved = BaseConfiguration.resolveDefaultInstallationDirectory(PlatformEnum.LINUX, fakeHome, Collections.emptyMap());
            assertEquals(linuxSteamCommon, resolved);
        }
    }

    @Nested
    class SystemPropertiesAndEnvironmentOverrides {

        @Test
        void systemPropertyTscInstallationDirectoryOverride(@TempDir Path tempDir) {
            String key = "tsc.installation.directory";
            String original = System.getProperty(key);
            try {
                System.setProperty(key, tempDir.toString());
                Path resolved = BaseConfiguration.resolveDefaultInstallationDirectory();
                assertEquals(tempDir.toAbsolutePath().normalize(), resolved);
            } finally {
                if (original != null) {
                    System.setProperty(key, original);
                } else {
                    System.clearProperty(key);
                }
            }
        }

        @Test
        void systemPropertyTscInstallDirOverride(@TempDir Path tempDir) {
            String key = "tsc.install.dir";
            String original = System.getProperty(key);
            try {
                System.setProperty(key, tempDir.toString());
                Path resolved = BaseConfiguration.resolveDefaultInstallationDirectory();
                assertEquals(tempDir.toAbsolutePath().normalize(), resolved);
            } finally {
                if (original != null) {
                    System.setProperty(key, original);
                } else {
                    System.clearProperty(key);
                }
            }
        }

        @Test
        void systemPropertyTscHomeOverride(@TempDir Path tempDir) {
            String key = "tsc.home";
            String original = System.getProperty(key);
            try {
                System.setProperty(key, tempDir.toString());
                Path resolved = BaseConfiguration.resolveDefaultInstallationDirectory();
                assertEquals(tempDir.toAbsolutePath().normalize(), resolved);
            } finally {
                if (original != null) {
                    System.setProperty(key, original);
                } else {
                    System.clearProperty(key);
                }
            }
        }
    }

    @Nested
    class DirectoryValidationAndNormalization {

        @Test
        void isValidInstallationDirectoryReturnsFalseForNullAndNonExistent() {
            assertFalse(BaseConfiguration.isValidInstallationDirectory(null));
            assertFalse(BaseConfiguration.isValidInstallationDirectory(Paths.get("/non/existent/path/for/tsc")));
        }

        @Test
        void isValidInstallationDirectoryReturnsTrueWhenExecutableExists(@TempDir Path tempDir) throws IOException {
            assertFalse(BaseConfiguration.isValidInstallationDirectory(tempDir));

            Path exe64 = tempDir.resolve("RailWorks64.exe");
            Files.createFile(exe64);
            assertTrue(BaseConfiguration.isValidInstallationDirectory(tempDir));
            Files.delete(exe64);

            Path exe32 = tempDir.resolve("RailWorks.exe");
            Files.createFile(exe32);
            assertTrue(BaseConfiguration.isValidInstallationDirectory(tempDir));
            Files.delete(exe32);

            Path assetsDir = tempDir.resolve("Assets");
            Files.createDirectory(assetsDir);
            assertTrue(BaseConfiguration.isValidInstallationDirectory(tempDir));
        }

        @Test
        void isInstallationExecutableRecognizesExpectedFileNames() {
            assertFalse(BaseConfiguration.isInstallationExecutable(null));
            assertFalse(BaseConfiguration.isInstallationExecutable(Paths.get("somethingElse.exe")));
            assertTrue(BaseConfiguration.isInstallationExecutable(Paths.get("RailWorks64.exe")));
            assertTrue(BaseConfiguration.isInstallationExecutable(Paths.get("RailWorks.exe")));
            assertTrue(BaseConfiguration.isInstallationExecutable(Paths.get("railworks64.exe")));
            assertTrue(BaseConfiguration.isInstallationExecutable(Paths.get("railworks.exe")));
            assertTrue(BaseConfiguration.isInstallationExecutable(Paths.get("C:/Games/RailWorks/RailWorks64.exe")));
        }

        @Test
        void normalizeInstallationPathReturnsNullForNull() {
            assertNull(BaseConfiguration.normalizeInstallationPath(null));
        }

        @Test
        void normalizeInstallationPathExtractsParentForExecutableFile(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("RailWorks64.exe");
            Files.createFile(file);

            Path normalized = BaseConfiguration.normalizeInstallationPath(file);
            assertEquals(tempDir.toAbsolutePath().normalize(), normalized);
        }
    }
}
