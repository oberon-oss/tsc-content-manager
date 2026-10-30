package eu.oberon.oss.tsc.cm.cli.tools;

import eu.oberonoss.tsc.cm.base.SERZTagTypes;
import eu.oberonoss.tsc.cm.shared.SERZTagDetector;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.LongAdder;

/**
 * Detects SERZ tags in binary files.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@CommandLine.Command(
        name = "serz-tag-detector",
        description = "Detects SERZ tags in binary files",
        mixinStandardHelpOptions = true
)
@Slf4j
public class SERZTagDetectorCLI implements Callable<Integer> {

    @CommandLine.Option(
            names = {"--file-or-directory", "-f"},
            description = "Single file or starting directory for scanning",
            required = true
    )
    Path startingDirectory;

    @CommandLine.ArgGroup()
    TagOptions tagOptions = new TagOptions();

    static class TagOptions {
        @CommandLine.Option(
                names = {"--id-bytes", "-i"},
                description = "Specify the 2nd byte in signature to include",
                split = ","
        )
        List<String> idBytes;

        @CommandLine.Option(
                names = {"--tag-patterns", "-u"},
                description = "Detect (potential) Tag patterns that match 0xFF 0x?? 0xFF 0xFF)"
        )
        boolean unknownTags;
    }

    @CommandLine.Option(
            names = {"--threads", "-t"},
            description = "Number of worker threads to use",
            defaultValue = "4"
    )
    private int threadCount;

    @CommandLine.Option(
            names = {"--include-known-tags", "-k"},
            description = "Include all known patterns: 0xFF 0x?? 0xFF 0xFF (41, 42, 43, 50, 56) and 0xFF 0x70",
            defaultValue = "false"
    )
    private boolean includeKnownTags;

    /**
     * Executes the SERZ starting tag detection process.
     *
     * @since 1.0.0
     */
    public SERZTagDetectorCLI() {
        // Keep Javadoc happy
    }

    /**
     * Executes the SERZ starting tag detection process.
     *
     * @param args Command line arguments.
     *
     * @since 1.0.0
     */
    static void main(String[] args) {
        System.exit(new CommandLine(new SERZTagDetectorCLI()).execute(args));
    }

    /**
     * Executes the SERZ starting tag detection process.
     *
     * @return The exit code of the command.
     *
     * @since 1.0.0
     */
    @Override
    public Integer call() {
        Set<Byte> targetBytes = getIDBytes();

        LOGGER.info("Starting SERZ tag detection process... {}", startingDirectory.toAbsolutePath());
        SERZTagDetector detector = new SERZTagDetector(threadCount, includeKnownTags);
        Map<Byte, LongAdder> aggregatedCounts = detector.detect(startingDirectory, targetBytes);

        // Let's calculate the TOTAL number of occurrences
        Iterator<Map.Entry<Byte, LongAdder>> iterator = aggregatedCounts.entrySet().iterator();
        long totalOccurrences = 0;

        while(iterator.hasNext()) {
            Map.Entry<Byte, LongAdder> next = iterator.next();
            totalOccurrences += next.getValue().sum();
        }

        for (Map.Entry<Byte, LongAdder> entry : aggregatedCounts.entrySet()) {
            byte tagId = entry.getKey();
            LOGGER.info("{}",String.format("Found %,15d occurrences of tag with ID 0x%02X %s (%3.2f%%)",
                    entry.getValue().sum(),
                    tagId,
                    SERZTagTypes.isKnownTagID(tagId) ? "(*)" : "   ",
                    (double) (entry.getValue().sum()) / totalOccurrences * 100.0)
            );
        }
        return 0;
    }

    Set<Byte> getIDBytes() {
        boolean unknown = tagOptions != null && tagOptions.unknownTags;
        List<String> sourceBytes = (tagOptions != null && tagOptions.idBytes != null) ? tagOptions.idBytes : null;
        return SERZTagDetector.getIDBytes(sourceBytes, unknown);
    }

}
