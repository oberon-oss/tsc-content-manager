package eu.oberon.oss.tsc.cm.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SERZTagDetectorTest {

    @Test
    void testExecutionWithClosingTag() {
        SERZTagDetector detector = new SERZTagDetector();
        Path testDir = Paths.get("../20-tsc-cm-io/src/test/resources/util/serzdetector");
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-d", testDir.toString(), "--include-closing-tag=true");
        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionWithoutClosingTag() {
        SERZTagDetector detector = new SERZTagDetector();
        Path testDir = Paths.get("../20-tsc-cm-io/src/test/resources/util/serzdetector");
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-d", testDir.toString(), "--include-closing-tag=false");
        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionWithCustomOptions() {
        SERZTagDetector detector = new SERZTagDetector();
        Path testDir = Paths.get("../20-tsc-cm-io/src/test/resources/util/serzdetector");
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-d", testDir.toString(), "-i", "0x50,0x56", "-t", "2", "-c");
        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionWithEmptyDirectory(@org.junit.jupiter.api.io.TempDir Path tempDir) {
        SERZTagDetector detector = new SERZTagDetector();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute("-d", tempDir.toString());
        assertEquals(0, exitCode);
    }

    @Test
    void testExecutionMissingRequiredOption() {
        SERZTagDetector detector = new SERZTagDetector();
        CommandLine cmd = new CommandLine(detector);
        int exitCode = cmd.execute();
        org.junit.jupiter.api.Assertions.assertNotEquals(0, exitCode);
    }
}
