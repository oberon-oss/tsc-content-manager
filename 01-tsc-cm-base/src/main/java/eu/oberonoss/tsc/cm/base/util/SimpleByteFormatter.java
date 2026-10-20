package eu.oberonoss.tsc.cm.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Provides functionality to format a byte array into a readable hexadecimal and character representation.
 * <p>
 * The formatted output includes a header, with hexadecimal offsets, the hexadecimal representation of the bytes, and an ASCII character representation for
 * printable characters. Non-printable characters are replaced with a dot ('.').
 * <p>
 * This class is not instantiable.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class SimpleByteFormatter {
    private SimpleByteFormatter() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleByteFormatter.class);

    private static final String HEX_HEADER = "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F";
    private static final String FILLER = " --- ";
    private static final String CHAR_HEADER = "0123456789ABCDEF";

    /**
     * Formats a byte array into a readable hexadecimal and character representation.
     * <p>
     * The output will start at offset 0. This is a convenience method, equal to calling {@link #formatBytes(byte[], int)} with an offset of 0.
     *
     * @param binaryData The byte array to format.
     *
     * @return The formatted string representation of the byte array.
     *
     * @since 1.0.0
     */
    public static String formatBytes(final byte[] binaryData) {
        return formatBytes(binaryData, 0);
    }

    /**
     * Formats a byte array into a readable hexadecimal and character representation.
     * <p>
     * This method allows the user control over the row/offset the data starts displaying on. For example, let's assume the following byte sequence:
     * </p>
     * <p>
     * {@code FF 43 00 38 00 00 00 }
     * </p>
     * <p>
     * If you called {@code formatBytes(new byte[]{(byte)0xFF, 0x43, 0x00, 0x38, 0x00, 0x00, 0x00}, 0)}, it would be displayed as:
     * </p>
     * <pre>
     * {@code
     *           00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F ---  0123456789ABCDEF
     *           -----------------------------------------------      ----------------
     * 00000000  FF 43 00 38 00 00 00                            --- [.C.8...         ]
     * }
     * </pre>
     * <p>
     * But let's now assume the data is part of a larger byte array and actually does NOT start at offset 0, but say 389. The output would then look like this:
     * </p>
     * <pre>
     * {@code
     *           00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F ---  0123456789ABCDEF
     *           -----------------------------------------------      ----------------
     * 00000180                 FF 43 00 38 00 00 00             --- [     .C.8...    ]
     * }
     * </pre>
     * <p>
     * As you can see, the offset is now displayed as 00000180 (decimal: 384) instead of 00000000, and the data starts at column 5 (384+5 == 389).
     * </p>
     *
     * @param binaryData  The byte array to format.
     * @param startOffset The offset to start formatting from.
     *
     * @return The formatted string representation of the byte array.
     *
     * @throws IllegalArgumentException if startOffset is negative.
     * @since 1.0.0
     */
    public static String formatBytes(final byte[] binaryData, int startOffset) {
        if (binaryData == null || binaryData.length == 0) {
            return "\n***** No data was specified *****\n";
        }

        if (startOffset < 0) {
            throw new IllegalArgumentException("startOffset must not be negative");
        }

        StringBuilder work = new StringBuilder(createHeader()).append("\n");

        int dataIndex = 0;
        int lineOffset = startOffset - (startOffset % 16);
        int columnOffset = startOffset % 16;

        while (dataIndex < binaryData.length) {
            int segmentLength = Math.min(16 - columnOffset, binaryData.length - dataIndex);
            byte[] segment = Arrays.copyOfRange(binaryData, dataIndex, dataIndex + segmentLength);

            LOGGER.debug("bytes remaining= {})", binaryData.length - dataIndex);
            work.append(String.format("%08X  ", lineOffset));
            work.append(format16ByteSegment(segment, columnOffset)).append("\n");

            dataIndex += segmentLength;
            lineOffset += 16;
            columnOffset = 0;
        }

        return work.append("\n").toString();
    }

    private static String format16ByteSegment(byte[] segment, int columnOffset) {
        LOGGER.debug("segment size= {})", segment.length);

        StringBuilder hexData = new StringBuilder();
        StringBuilder charData = new StringBuilder("[");

        for (int column = 0; column < 16; column++) {
            int segmentIndex = column - columnOffset;

            if (segmentIndex >= 0 && segmentIndex < segment.length) {
                byte value = segment[segmentIndex];

                hexData.append(String.format("%02X", value));

                if (value >= 32 && value <= 126) {
                    charData.append((char) value);
                } else {
                    charData.append(".");
                }
            } else {
                hexData.append("  ");
                charData.append(" ");
            }

            if (column < 15) {
                hexData.append(" ");
            }
        }

        return hexData
                .append(FILLER)
                .append(charData.append("]"))
                .toString();
    }


    private static String createHeader() {
        return " ".repeat(10) +
                HEX_HEADER +
                FILLER +
                " " + CHAR_HEADER +
                "\n" +
                " ".repeat(10) +
                "-".repeat(HEX_HEADER.length()) +
                " ".repeat(FILLER.length()) +
                " " +
                "-".repeat(CHAR_HEADER.length())
                ;
    }

}
