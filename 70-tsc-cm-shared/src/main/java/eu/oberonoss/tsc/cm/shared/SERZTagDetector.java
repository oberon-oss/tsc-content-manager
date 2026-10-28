package eu.oberonoss.tsc.cm.shared;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberonoss.tsc.cm.base.GlobalConstants;
import eu.oberonoss.tsc.cm.io.util.BinaryFileProcessor;
import eu.oberonoss.tsc.cm.io.util.FileProcessor;
import eu.oberonoss.tsc.cm.io.util.FileScanner;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.oberonoss.tsc.cm.base.util.HardwareDetails.HardwareDetailItem.CPU_CORE_COUNT;

/**
 * Detects and aggregates SERZ tags in binary files.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Slf4j
public class SERZTagDetector {

    /**
     * Marker byte (0xFF) used to identify SERZ tags.
     *
     * @since 1.0.0
     */
    public static final byte MARKER_BYTE = GlobalConstants.MARKER_BYTE;

    /**
     * Byte value (0x70) representing a SERZ closing tag.
     *
     * @since 1.0.0
     */
    public static final byte CLOSING_TAG_BYTE = GlobalConstants.CLOSING_TAG_BYTE;

    /**
     * Default list of hex string representations for standard SERZ tag IDs.
     *
     * @since 1.0.0
     */
    public static final List<String> DEFAULT_ID_BYTES = List.of("0x50", "0x56", "0x41", "0x42", "0x43");

    private static final Pattern PATTERN = Pattern.compile("0x([0-9A-F]{2})", Pattern.CASE_INSENSITIVE);

    private final int threadCount;
    private final boolean includeClosingTag;
    private final ConcurrentHashMap<Byte, LongAdder> aggregatedCounts = new ConcurrentHashMap<>();

    /**
     * Creates a new SERZTagDetector with default {@link eu.oberonoss.tsc.cm.base.util.HardwareDetails.HardwareDetailItem#CPU_CORE_COUNT} threads and closing
     * tag inclusion enabled.
     *
     * @since 1.0.0
     */
    public SERZTagDetector() {
        this(CPU_CORE_COUNT.getInfo(), true);
    }

    /**
     * Creates a new SERZTagDetector with the specified thread count and default closing tag inclusion enabled.
     *
     * @param threadCount The number of worker threads to use.
     *
     * @since 1.0.0
     */
    public SERZTagDetector(int threadCount) {
        this(threadCount, true);
    }

    /**
     * Creates a new SERZTagDetector with the specified closing tag inclusion setting and default
     * {@link eu.oberonoss.tsc.cm.base.util.HardwareDetails.HardwareDetailItem#CPU_CORE_COUNT} worker threads.
     *
     * @param includeClosingTag Whether to include closing tags in detection.
     *
     * @since 1.0.0
     */
    public SERZTagDetector(boolean includeClosingTag) {
        this(CPU_CORE_COUNT.getInfo(), includeClosingTag);
    }

    /**
     * Creates a new SERZTagDetector with the specified thread count and closing tag inclusion setting.
     *
     * @param threadCount       The number of worker threads to use.
     * @param includeClosingTag Whether to include closing tags in detection.
     *
     * @since 1.0.0
     */
    public SERZTagDetector(int threadCount, boolean includeClosingTag) {
        this.threadCount = threadCount;
        this.includeClosingTag = includeClosingTag;
    }

    /**
     * Scans the specified directory or file for SERZ tags matching the provided target byte IDs and returns the aggregated counts.
     *
     * @param startingDirectory Single file or directory to scan.
     * @param targetBytes       Set of target byte IDs to detect.
     *
     * @return The aggregated counts of detected tags mapped by tag ID byte.
     *
     * @since 1.0.0
     */
    public Map<Byte, LongAdder> detect(Path startingDirectory, Set<Byte> targetBytes) {
        FileProcessor<BinaryDataReader> processor = new BinaryFileProcessor(reader -> patternScanner(reader, targetBytes));
        FileScanner scanner = FileScanner.of(threadCount);
        scanner.scan(processor, startingDirectory);
        return aggregatedCounts;
    }

    /**
     * Scans the specified directory or file for default SERZ tags and returns the aggregated counts.
     *
     * @param startingDirectory Single file or directory to scan.
     *
     * @return The aggregated counts of detected tags mapped by tag ID byte.
     *
     * @since 1.0.0
     */
    public Map<Byte, LongAdder> detect(Path startingDirectory) {
        return detect(startingDirectory, getIDBytes(null, false));
    }

    /**
     * Scans the specified directory or file using tag options and returns the aggregated counts.
     *
     * @param startingDirectory Single file or directory to scan.
     * @param idBytes           Custom list of hex ID strings or null to use defaults.
     * @param unknownTags       Whether to detect unknown tags.
     *
     * @return The aggregated counts of detected tags mapped by tag ID byte.
     *
     * @since 1.0.0
     */
    public Map<Byte, LongAdder> detect(Path startingDirectory, List<String> idBytes, boolean unknownTags) {
        return detect(startingDirectory, getIDBytes(idBytes, unknownTags));
    }

    /**
     * Resolves the target ID bytes based on custom ID strings and unknown tags flag.
     *
     * @param idBytes     List of hex string representations of ID bytes, or null.
     * @param unknownTags Whether to include all unknown tags.
     *
     * @return The resolved set of target ID bytes.
     *
     * @since 1.0.0
     */
    public static Set<Byte> getIDBytes(List<String> idBytes, boolean unknownTags) {
        if (unknownTags) {
            Set<Byte> known = parseIdBytes(DEFAULT_ID_BYTES);
            Set<Byte> unknown = new HashSet<>();
            for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
                byte val = (byte) b;
                if (!known.contains(val) && val != MARKER_BYTE) {
                    unknown.add(val);
                }
            }
            return unknown;
        }

        List<String> sourceBytes = (idBytes != null) ? idBytes : DEFAULT_ID_BYTES;
        return parseIdBytes(sourceBytes);
    }

    /**
     * Parses a list of hex string representations (e.g. "0x50") into a set of bytes.
     *
     * @param source The list of hex strings.
     *
     * @return A set of parsed bytes.
     *
     * @since 1.0.0
     */
    public static Set<Byte> parseIdBytes(List<String> source) {
        Set<Byte> temp = new HashSet<>();
        if (source != null) {
            for (String idByte : source) {
                Matcher m = PATTERN.matcher(idByte);
                if (m.matches()) {
                    temp.add((byte) Integer.parseInt(m.group(1), 16));
                }
            }
        }
        return temp;
    }

    private void patternScanner(BinaryDataReader reader, Set<Byte> targetBytes) {
        BinaryDataViewer viewer = reader.getViewer();
        byte[] section = new byte[4]; // Local to thread execution

        while (reader.remaining() > 0) {
            int step = 1;
            if (viewer.peekByte(reader.offset()) == MARKER_BYTE) {
                if (reader.remaining() >= 4) {
                    viewer.peekBytes(section, reader.offset());
                    if (targetBytes.contains(section[1]) && section[2] == MARKER_BYTE && section[3] == MARKER_BYTE) {
                        aggregatedCounts.computeIfAbsent(section[1], _ -> new LongAdder()).increment();
                        step = 4;
                    } else if (includeClosingTag && section[1] == CLOSING_TAG_BYTE) {
                        aggregatedCounts.computeIfAbsent(section[1], _ -> new LongAdder()).increment();
                        step = 2;
                    }
                } else if (includeClosingTag && reader.remaining() >= 2 && viewer.peekByte(reader.offset() + 1) == CLOSING_TAG_BYTE) {
                    aggregatedCounts.computeIfAbsent(CLOSING_TAG_BYTE, _ -> new LongAdder()).increment();
                    step = 2;
                }
            }
            reader.offset(reader.offset() + step);
        }
    }

}
