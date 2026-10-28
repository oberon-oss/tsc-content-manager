package eu.oberonoss.tsc.cm.base;

/**
 * Global constant definitions for SERZ processing.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class GlobalConstants {

    private GlobalConstants() {
    }

    /**
     * Marker byte (0xFF) used to identify SERZ tags.
     *
     * @since 1.0.0
     */
    public static final byte MARKER_BYTE = (byte) 0xFF;

    /**
     * Byte value (0x70) representing a SERZ closing tag.
     *
     * @since 1.0.0
     */
    public static final byte CLOSING_TAG_BYTE = (byte) 0x70;

}
