package eu.oberonoss.tsc.cm.base;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;

/**
 * Enum representing different SERZ tag types.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public enum SERZTagTypes {
    /**
     * Not a tag as such, but added for completeness.
     *
     * @since 1.0.0
     */
    FILE_HEADER(new byte[]{0x53, 0x45, 0x52, 0x5A, 0x00, 0x00, 0x01, 0x00}),
    /**
     * Type 0x41 (65 decimal)
     *
     * @since 1.0.0
     */
    TYPE_X41(new byte[]{(byte) 0xFF, 0x41, (byte) 0xFF, (byte) 0xFF}),
    /**
     * Type 0x42 (66 decimal)
     *
     * @since 1.0.0
     */
    TYPE_X42(new byte[]{(byte) 0xFF, 0x42, (byte) 0xFF, (byte) 0xFF}),
    /**
     * Type 0x43 (67 decimal)
     *
     * @since 1.0.0
     */
    TYPE_X43(new byte[]{(byte) 0xFF, 0x43, (byte) 0xFF, (byte) 0xFF}),
    /**
     * Type 0x50 (80 decimal)
     *
     * @since 1.0.0
     */
    TYPE_X50(new byte[]{(byte) 0xFF, 0x50, (byte) 0xFF, (byte) 0xFF}),
    /**
     * Type 0x56 (86 decimal)
     *
     * @since 1.0.0
     */
    TYPE_X56(new byte[]{(byte) 0xFF, 0x56, (byte) 0xFF, (byte) 0xFF}),

    /**
     * Type 0x70 (112 decimal) - this most likely represents a close tag.
     *
     * @since 1.0.0
     */
    TYPE_X70(new byte[]{(byte) 0xFF, (byte) 0x70});

    private final byte[] bytes;

    SERZTagTypes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Checks if the data at the current offset in the reader matches the bytes of this tag type.
     *
     * @param reader The reader to check.
     *
     * @return True if the reader matches, false otherwise.
     *
     * @since 1.0.0
     */
    public boolean matches(BinaryDataReader reader) {
        return reader.matches(bytes);
    }

    /**
     * Returns the size of the byte array representing this tag type.
     *
     * @return The size of the byte array.
     *
     * @since 1.0.0
     */
    public int tagByteArraySize() {
        return bytes.length;
    }
}
