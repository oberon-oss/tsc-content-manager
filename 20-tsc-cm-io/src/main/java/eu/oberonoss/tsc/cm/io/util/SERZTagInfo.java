package eu.oberonoss.tsc.cm.io.util;

import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberonoss.tsc.cm.base.SERZTagTypes;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents information about a tagged section in a binary SERZ file.
 * <p>
 * This class is used to store metadata about a specific tag and its associated data within a binary SERZ file. Tags are identifiable structures in the file,
 * described by their type, location, and size, which are captured by this class.
 * <p>
 * Instances of this class are typically constructed using its builder, enabled by the Lombok @Builder annotation.
 * <p>
 * Immutable and thread-safe due to final fields.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@SuppressWarnings({"JavadocDeclaration", "ClassCanBeRecord"})
@Builder(builderClassName = "SERZTagInfoBuilder")
@ToString(exclude = "viewer")
@Getter
public class SERZTagInfo {
    // @formatter:off
    /**
     * The tag type.
     * --- SETTER ---
     * Sets the tag type.
     *
     * @param tagType the tag type to set
     * @return {@code this} builder instance
     * --- GETTER ---
     * Returns the tag type.
     *
     * @return the tag type
     * @since 1.0.0
     */
    private final SERZTagTypes tagType;

    /**
     * The binary data viewer.
     * --- SETTER ---
     * Sets the binary data viewer.
     *
     * @param viewer the binary data viewer to set
     * @return {@code this} builder instance
     * --- GETTER ---
     * Returns the binary data viewer.
     *
     * @return the binary data viewer
     * @since 1.0.0
     */
    private final BinaryDataViewer viewer;

    /**
     * The byte offset where the tag starts.
     * --- SETTER ---
     * Sets the byte offset where the tag starts.
     *
     * @param tagOffset the byte offset where the tag starts
     * @return {@code this} builder instance
     * --- GETTER ---
     * Returns the byte offset where the tag starts.
     *
     * @return the byte offset where the tag starts
     * @since 1.0.0
     */
    private final int tagOffset;

    /**
     * The byte offset where the tag data starts.
     * --- SETTER ---
     * Sets the byte offset where the tag data starts.
     *
     * @param dataOffset the byte offset where the tag data starts
     * @return {@code this} builder instance
     * --- GETTER ---
     * Returns the byte offset where the tag data starts.
     *
     * @return the byte offset where the tag data starts
     * @since 1.0.0
     */
    private final int dataOffset;

    /**
     * The length of the data associated with the tag.
     * --- SETTER ---
     * Sets the length of the data associated with the tag.
     *
     * @param dataLength the length of the tag data in bytes
     * @return {@code this} builder instance
     * --- GETTER ---
     * Returns the length of the data associated with the tag.
     *
     * @return the length of the tag data in bytes
     * @since 1.0.0
     */
    private final int dataLength;
    // @formatter:on

    /**
     * Constructs a new {@code SERZTagInfo} instance.
     *
     * @param tagType    the tag type
     * @param viewer     the binary data viewer
     * @param tagOffset  the byte offset where the tag starts
     * @param dataOffset the byte offset where the tag data starts
     * @param dataLength the length of the tag data in bytes
     *
     * @since 1.0.0
     */
    public SERZTagInfo(SERZTagTypes tagType, BinaryDataViewer viewer, int tagOffset, int dataOffset, int dataLength) {
        this.tagType = tagType;
        this.viewer = viewer;
        this.tagOffset = tagOffset;
        this.dataOffset = dataOffset;
        this.dataLength = dataLength;
    }

    /**
     * Creates a new builder for {@link SERZTagInfo}.
     *
     * @return a new {@code SERZTagInfoBuilder} instance
     *
     * @since 1.0.0
     */
    public static SERZTagInfoBuilder builder() {
        return new SERZTagInfoBuilder();
    }

    /**
     * Builder for {@link SERZTagInfo} instances.
     *
     * @since 1.0.0
     */
    public static class SERZTagInfoBuilder {
        /**
         * Builds a new {@link SERZTagInfo} instance.
         *
         * @return a new {@code SERZTagInfo} instance
         *
         * @since 1.0.0
         */
        public SERZTagInfo build() {
            return new SERZTagInfo(this.tagType, this.viewer, this.tagOffset, this.dataOffset, this.dataLength);
        }
    }
}
