package eu.oberon.oss.tsc.cm.cli.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SERZTagDetectorCLITest {

    private static final Path TEST_DIR = Paths.get("../20-tsc-cm-io/src/test/resources/util/serzdetector");

    @ParameterizedTest
    @ValueSource(strings = {"-k", "--include-known-tags", "--include-known-tags=true", "--include-known-tags=false"})
    void testExecutionWithKnownTagsOption(String knownTagsOption) {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-f", TEST_DIR.toString(), knownTagsOption);
        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionWithCustomOptions() {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-f", TEST_DIR.toString(), "-i", "0x50,0x56", "-t", "2", "-k");
        assertEquals(0, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-f {dir} -i 0x50,0x56 -t 2 -k",
            "--file-or-directory {dir} --id-bytes=0x50,0x56 --threads=2 --include-known-tags",
            "-f {dir} -u -t 2 -k",
            "--file-or-directory {dir} --tag-patterns --threads=4 --include-known-tags",
            "-f {dir} -k",
            "-f {dir} --include-known-tags=true",
            "-f {dir} --include-known-tags=false",
            "--file-or-directory {dir} --threads=1",
            "-f {dir} -i 0x50",
            "-f {dir} -u"
    })
    void testExecutionWithValidFlagCombinations(String commandLineArgs) {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        String formatted = commandLineArgs.replace("{dir}", TEST_DIR.toString());
        String[] args = formatted.split(" ");
        int exitCode = cmd.execute(args);
        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionWithEmptyDirectory(@TempDir Path tempDir) {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-f", tempDir.toString());
        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionMissingRequiredOption() {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute();
        assertNotEquals(0, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-u", "--tag-patterns"})
    void testExecutionWithTagPatternsOption(String tagPatternsOption) {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-f", TEST_DIR.toString(), tagPatternsOption);
        assertEquals(0, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-f {dir} -i 0x50 -u",
            "-f {dir} --id-bytes=0x50 --tag-patterns",
            "-f {dir} -u -i 0x50",
            "-f {dir} --tag-patterns --id-bytes=0x50"
    })
    void testExecutionWithMutuallyExclusiveOptions(String commandLineArgs) {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        String formatted = commandLineArgs.replace("{dir}", TEST_DIR.toString());
        String[] args = formatted.split(" ");
        int exitCode = cmd.execute(args);
        assertNotEquals(0, exitCode);
    }

    @ParameterizedTest
    @ValueSource(ints = {0xFF, 0x50, 0x56, 0x41, 0x42, 0x43})
    void testUnknownTagsExcludesDefaultAndSpecialIds(int id) {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        cmd.parseArgs("-f", "dummy", "-u");
        Set<Byte> idBytes = detector.getIDBytes();

        assertFalse(idBytes.contains((byte) id), () -> String.format("0x%02X should be excluded from unknown tags", id));
    }

    @Test
    void testUnknownTagsTotalCount() {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        cmd.parseArgs("-f", "dummy", "-u");
        Set<Byte> idBytes = detector.getIDBytes();

        assertEquals(256 - 5 - 1, idBytes.size());
    }

    @Test
    void testLoggerTopic() throws NoSuchFieldException, IllegalAccessException {
        Field loggerField = SERZTagDetectorCLI.class.getDeclaredField("LOGGER");
        loggerField.setAccessible(true);
        org.slf4j.Logger logger = (org.slf4j.Logger) loggerField.get(null);
        assertEquals("serz-tag-detector", logger.getName());
    }
}
