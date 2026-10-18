package eu.oberonoss.tsc.cm.io.util;

import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import eu.oberonoss.tsc.cm.base.SERZTagTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SERZTagInfoTest {

    @Test
    void testConstructorAndGetters() {
        BinaryDataViewer viewer = new BinaryDataViewerImpl(new byte[]{1, 2, 3});
        SERZTagInfo info = new SERZTagInfo(SERZTagTypes.FILE_HEADER, viewer, 0, 8, 16);

        assertEquals(SERZTagTypes.FILE_HEADER, info.getTagType());
        assertSame(viewer, info.getViewer());
        assertEquals(0, info.getTagOffset());
        assertEquals(8, info.getDataOffset());
        assertEquals(16, info.getDataLength());
    }

    @Test
    void testBuilder() {
        BinaryDataViewer viewer = new BinaryDataViewerImpl(new byte[]{4, 5, 6});
        SERZTagInfo info = SERZTagInfo.builder()
                .tagType(SERZTagTypes.TYPE_X50)
                .viewer(viewer)
                .tagOffset(100)
                .dataOffset(104)
                .dataLength(20)
                .build();

        assertNotNull(info);
        assertEquals(SERZTagTypes.TYPE_X50, info.getTagType());
        assertSame(viewer, info.getViewer());
        assertEquals(100, info.getTagOffset());
        assertEquals(104, info.getDataOffset());
        assertEquals(20, info.getDataLength());
    }

    @Test
    void testToStringExcludesViewer() {
        BinaryDataViewer viewer = new BinaryDataViewerImpl(new byte[]{1});
        SERZTagInfo info = SERZTagInfo.builder()
                .tagType(SERZTagTypes.TYPE_X70)
                .viewer(viewer)
                .tagOffset(10)
                .dataOffset(12)
                .dataLength(4)
                .build();

        String str = info.toString();
        assertNotNull(str);
        assertTrue(str.contains("TYPE_X70"));
        assertTrue(str.contains("tagOffset=10"));
        assertTrue(str.contains("dataOffset=12"));
        assertTrue(str.contains("dataLength=4"));
        assertFalse(str.contains("viewer="));
    }
}
