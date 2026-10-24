package eu.oberonoss.tsc.cm.base;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataReaderImpl;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class SERZTagTypesTest {

    @Test
    void testTagByteArraySizes() {
        assertEquals(8, SERZTagTypes.FILE_HEADER.tagByteArraySize());
        assertEquals(4, SERZTagTypes.TYPE_X41.tagByteArraySize());
        assertEquals(4, SERZTagTypes.TYPE_X42.tagByteArraySize());
        assertEquals(4, SERZTagTypes.TYPE_X43.tagByteArraySize());
        assertEquals(4, SERZTagTypes.TYPE_X50.tagByteArraySize());
        assertEquals(4, SERZTagTypes.TYPE_X56.tagByteArraySize());
        assertEquals(2, SERZTagTypes.TYPE_X70.tagByteArraySize());
    }

    @Test
    void testMatchesFileHeader() {
        byte[] validHeader = {0x53, 0x45, 0x52, 0x5A, 0x00, 0x00, 0x01, 0x00};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(validHeader));

        assertTrue(SERZTagTypes.FILE_HEADER.matches(reader));
        assertFalse(SERZTagTypes.TYPE_X50.matches(reader));
    }

    @Test
    void testMatchesTypeX50() {
        byte[] x50 = {(byte) 0xFF, 0x50, (byte) 0xFF, (byte) 0xFF};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(x50));

        assertTrue(SERZTagTypes.TYPE_X50.matches(reader));
        assertFalse(SERZTagTypes.TYPE_X56.matches(reader));
        assertFalse(SERZTagTypes.FILE_HEADER.matches(reader));
    }

    @Test
    void testMatchesTypeX70() {
        byte[] x70 = {(byte) 0xFF, (byte) 0x70};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(x70));

        assertTrue(SERZTagTypes.TYPE_X70.matches(reader));
        assertFalse(SERZTagTypes.TYPE_X41.matches(reader));
    }

    @ParameterizedTest
    @EnumSource(SERZTagTypes.class)
    void testEnumValuesNotNull(SERZTagTypes tagType) {
        assertNotNull(tagType.name());
        assertTrue(tagType.tagByteArraySize() > 0);
    }
}
