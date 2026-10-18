package eu.oberonoss.tsc.cm.io.util;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberonoss.tsc.cm.base.ByteLoaderImpl;
import eu.oberonoss.tsc.cm.base.SERZTagTypes;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Scans binary SERZ data and extracts information about tags.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Slf4j
public class BinarySERZDataScanner {

    /**
     * Default constructor.
     *
     * @since 1.0.0
     */
    public BinarySERZDataScanner() {
        // Keep Javadoc happy
    }

    /**
     * Scans binary SERZ data and extracts information about tags.
     *
     * @param inputStream The input stream containing binary SERZ data.
     *
     * @return A list of SERZTagInfo objects representing the extracted tag information.
     *
     * @throws IOException If an I/O error occurs while reading the input stream.
     * @since 1.0.0
     */
    public List<SERZTagInfo> scanBinarySERZData(InputStream inputStream) throws IOException {
        BinaryDataReader reader = ByteLoaderImpl.getInstance().getBinaryDataReader(inputStream);
        List<SERZTagInfo> serzTagInfos = new LinkedList<>();

        SERZTagInfo.SERZTagInfoBuilder builder = null;
        int currentDataOffset = 0;

        while (reader.remaining() > 0) {
            SERZTagTypes matchedTag = findNextTag(reader);
            if (matchedTag != null) {
                if (builder != null) {
                    builder.dataLength(reader.offset() - currentDataOffset);
                    serzTagInfos.add(builder.build());
                }
                int tagOffset = reader.offset();
                currentDataOffset = tagOffset + matchedTag.tagByteArraySize();

                builder = SERZTagInfo.builder()
                        .tagType(matchedTag)
                        .viewer(reader.getViewer())
                        .tagOffset(tagOffset)
                        .dataOffset(currentDataOffset);

                reader.offset(currentDataOffset);
            }
        }

        if (builder != null) {
            builder.dataLength(reader.getViewer().size() - currentDataOffset);
            serzTagInfos.add(builder.build());
        }

        return serzTagInfos;
    }

    private SERZTagTypes findNextTag(BinaryDataReader reader) {
        while (reader.remaining() > 0) {
            for (SERZTagTypes tagType : SERZTagTypes.values()) {
                if (tagType.matches(reader)) {
                    LOGGER.debug(String.format("Found @ 0x%04X, type=%s ", reader.offset(), tagType));
                    return tagType;
                }
            }
            reader.offset(reader.offset() + 1);
        }
        return null;
    }

}
