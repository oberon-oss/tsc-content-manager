package eu.oberon.oss.tsc.cm.cli;


import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberonoss.tsc.cm.io.util.BinaryFileProcessor;
import eu.oberonoss.tsc.cm.io.util.FileProcessor;
import eu.oberonoss.tsc.cm.io.util.FileScanner;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects SERZ tags in binary files.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@CommandLine.Command(
        name = "serz-tag-detector",
        description = "Detects SERZ tags in binary files"
)
@Slf4j
public class SERZTagDetector implements Callable<Integer> {

    @CommandLine.Option(
            names = {"--file-or-directory", "-f"},
            description = "Single file or starting directory for scanning",
            required = true
    )
    Path startingDirectory;

    @CommandLine.Option(
            names = {"--id-bytes", "-i"},
            description = "Specify the 2nd byte in signature to include",
            defaultValue = "0x50,0x56,0x41,0x42,0x43",
            split = ","
    )
    List<String> idBytes;

    @CommandLine.Option(
            names = {"--threads", "-t"},
            description = "Number of worker threads to use",
            defaultValue = "4"
    )
    private int threadCount;

    @CommandLine.Option(
            names = {"--include-closing-tag", "-c"},
            description = "Include closing tag (0x70) in detection",
            defaultValue = "true"
    )
    private boolean includeClosingTag;

    private static final byte MARKER_BYTE = (byte) 0xFF;
    private static final byte CLOSING_TAG_BYTE = (byte) 0x70;

    private final ConcurrentHashMap<Byte, LongAdder> aggregatedCounts = new ConcurrentHashMap<>();
    private final LongAdder processedFiles = new LongAdder();


    /**
     * Executes the SERZ starting tag detection process.
     *
     * @since 1.0.0
     */
    public SERZTagDetector() {
        // Keep Javadoc happy
    }

    /**
     * Executes the SERZ starting tag detection process.
     *
     * @since 1.0.0
     */
    static void main(String[] args) {
        System.exit(new CommandLine(new SERZTagDetector()).execute(args));
    }

    /**
     * Executes the SERZ starting tag detection process.
     *
     * @return The exit code of the command.
     *
     * @since 1.0.0
     */
    @SuppressWarnings("StringConcatenationArgumentToLogCall")
    @Override
    public Integer call() {
        Set<Byte> targetBytes = getIDBytes();

        LOGGER.info("Starting SERZ tag detection process... {}", startingDirectory.toAbsolutePath());
        FileProcessor<BinaryDataReader> processor = new BinaryFileProcessor(reader -> patternScanner(reader, targetBytes));
        FileScanner scanner = FileScanner.of(threadCount);
        scanner.scan(processor, startingDirectory);

        for (Map.Entry<Byte, LongAdder> entry : aggregatedCounts.entrySet()) {
            LOGGER.info(String.format("Found %,15d occurrences of tag with ID 0x%02X", entry.getValue().sum(), entry.getKey()));
        }
        return 0;
    }

    private void patternScanner(BinaryDataReader reader, Set<Byte> targetBytes) {
        BinaryDataViewer viewer = reader.getViewer();
        byte[] section = new byte[4]; // Local to thread execution

        while (reader.remaining() > 0) {
            if (viewer.peekByte(reader.offset()) == MARKER_BYTE) {
                viewer.peekBytes(section, reader.offset());
                if (targetBytes.contains(section[1]) && section[2] == MARKER_BYTE && section[3] == MARKER_BYTE) {
                    aggregatedCounts.computeIfAbsent(section[1], _ -> new LongAdder()).increment();
                    reader.offset(reader.offset() + 4);
                } else if (includeClosingTag && section[1] == CLOSING_TAG_BYTE) {
                    aggregatedCounts.computeIfAbsent(section[1], _ -> new LongAdder()).increment();
                    reader.offset(reader.offset() + 2);
                }
            }
            reader.offset(reader.offset() + 1);
        }

        processedFiles.increment();
        long current = processedFiles.sum();
        if (current % 5000 == 0) {
            LOGGER.info("Processed {} files", current);
        }
    }

    private static final Pattern PATTERN = Pattern.compile("0x([0-9A-F]{2})", Pattern.CASE_INSENSITIVE);

    private Set<Byte> getIDBytes() {
        Set<Byte> temp = new HashSet<>();
        if (idBytes != null) {
            for (String idByte : idBytes) {
                Matcher m = PATTERN.matcher(idByte);
                if (m.matches()) {
                    temp.add(Byte.parseByte(m.group(1), 16));
                }
            }
        }
        return temp;
    }

}
