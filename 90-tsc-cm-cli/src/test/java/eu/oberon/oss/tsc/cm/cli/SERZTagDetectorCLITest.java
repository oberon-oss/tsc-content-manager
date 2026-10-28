package eu.oberon.oss.tsc.cm.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SERZTagDetectorCLITest {

    private static final Path TEST_DIR = Paths.get("../20-tsc-cm-io/src/test/resources/util/serzdetector");

    @ParameterizedTest
    @ValueSource(strings = {"--include-closing-tag=true", "--include-closing-tag=false"})
    void testExecutionWithClosingTagOption(String closingTagOption) {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-f", TEST_DIR.toString(), closingTagOption);
        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionWithCustomOptions() {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-f", TEST_DIR.toString(), "-i", "0x50,0x56", "-t", "2", "-c");
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
    @ValueSource(strings = {"-u", "--unknown-tags"})
    void testExecutionWithUnknownTagsOption(String unknownTagsOption) {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-f", TEST_DIR.toString(), unknownTagsOption);
        assertEquals(0, exitCode);
    }

    @ParameterizedTest
    @CsvSource({
            "-i, 0x50, -u",
            "--id-bytes, 0x50, --unknown-tags"
    })
    void testExecutionWithMutuallyExclusiveOptions(String idOption, String idValue, String unknownTagOption) {
        SERZTagDetectorCLI detector = new SERZTagDetectorCLI();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-f", TEST_DIR.toString(), idOption, idValue, unknownTagOption);
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
}
