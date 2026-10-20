package eu.oberonoss.tsc.cm.base.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class SimpleByteFormatterTest {

    private static final String NO_DATA_MESSAGE = "\n***** No data was specified *****\n";

    @Nested
    class Construction {

        @Test
        void constructorIsPrivate() throws NoSuchMethodException {
            Constructor<SimpleByteFormatter> constructor = SimpleByteFormatter.class.getDeclaredConstructor();

            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }

        @Test
        void privateConstructorCanBeInvokedReflectively() throws NoSuchMethodException {
            Constructor<SimpleByteFormatter> constructor = SimpleByteFormatter.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertDoesNotThrow((Executable) constructor::newInstance);
        }
    }

    @Nested
    class NoData {

        @Test
        void formatBytesReturnsNoDataMessageForNullInput() {
            assertEquals(NO_DATA_MESSAGE, SimpleByteFormatter.formatBytes(null));
        }

        @Test
        void formatBytesReturnsNoDataMessageForEmptyInput() {
            assertEquals(NO_DATA_MESSAGE, SimpleByteFormatter.formatBytes(new byte[0]));
        }
    }

    @Nested
    class Formatting {

        @Test
        void formatBytesIncludesHeader() {
            String result = SimpleByteFormatter.formatBytes(new byte[]{0x01});

            assertTrue(result.contains("00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F"));
            assertTrue(result.contains("0123456789ABCDEF"));
            assertTrue(result.contains("-----------------------------------------------"));
            assertTrue(result.contains("----------------"));
        }

        @Test
        void formatBytesUsesZeroAsDefaultStartOffset() {
            String result = SimpleByteFormatter.formatBytes(new byte[]{0x01});

            assertTrue(result.contains("00000000  01"));
            assertTrue(result.contains("[.               ]"));
        }

        @Test
        void formatBytesFormatsSingleFullSixteenByteLine() {
            byte[] bytes = new byte[]{
                    0x00,
                    0x01,
                    0x02,
                    0x03,
                    0x04,
                    0x05,
                    0x06,
                    0x07,
                    0x08,
                    0x09,
                    0x0A,
                    0x0B,
                    0x0C,
                    0x0D,
                    0x0E,
                    0x0F
            };

            String result = SimpleByteFormatter.formatBytes(bytes);

            assertTrue(result.contains("00000000  00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F"));
            assertTrue(result.contains("[................]"));
        }

        @Test
        void formatBytesFormatsPrintableAsciiCharacters() {
            byte[] bytes = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

            String result = SimpleByteFormatter.formatBytes(bytes);

            assertTrue(result.contains("00000000  30 31 32 33 34 35 36 37 38 39 41 42 43 44 45 46"));
            assertTrue(result.contains("[0123456789ABCDEF]"));
        }

        @Test
        void formatBytesReplacesNonPrintableCharactersWithDots() {
            byte[] bytes = new byte[]{
                    0x1F,
                    0x20,
                    0x41,
                    0x7E,
                    0x7F
            };

            String result = SimpleByteFormatter.formatBytes(bytes);

            assertTrue(result.contains("00000000  1F 20 41 7E 7F"));
            assertTrue(result.contains("[. A~."));
        }

        @Test
        void formatBytesPadsPartialLineToSixteenCharactersInCharacterView() {
            byte[] bytes = "ABC".getBytes(StandardCharsets.US_ASCII);

            String result = SimpleByteFormatter.formatBytes(bytes);

            assertTrue(result.contains("00000000  41 42 43"));
            assertTrue(result.contains("[ABC             ]"));
        }

        @Test
        void formatBytesFormatsMultipleLinesWithIncreasingOffsets() {
            byte[] bytes = "0123456789ABCDEFGHIJKLMNOP".getBytes(StandardCharsets.US_ASCII);

            String result = SimpleByteFormatter.formatBytes(bytes);

            assertTrue(result.contains("00000000  30 31 32 33 34 35 36 37 38 39 41 42 43 44 45 46"));
            assertTrue(result.contains("[0123456789ABCDEF]"));
            assertTrue(result.contains("00000010  47 48 49 4A 4B 4C 4D 4E 4F 50"));
            assertTrue(result.contains("[GHIJKLMNOP      ]"));
        }

        @Test
        void formatBytesUsesAlignedCustomStartOffsetAsLineOffset() {
            byte[] bytes = "ABC".getBytes(StandardCharsets.US_ASCII);

            String result = SimpleByteFormatter.formatBytes(bytes, 0x00000B10);

            assertTrue(result.contains("00000B10  41 42 43"));
            assertTrue(result.contains("[ABC             ]"));
        }

        @Test
        void formatBytesAlignsUnalignedCustomStartOffsetToPreviousSixteenByteBoundary() {
            byte[] bytes = new byte[]{
                    (byte) 0xFF,
                    0x43,
                    0x00,
                    0x38,
                    0x00,
                    0x00,
                    0x00
            };

            String result = SimpleByteFormatter.formatBytes(bytes, 2839);

            assertTrue(result.contains("00000B10                       FF 43 00 38 00 00 00"));
            assertTrue(result.contains("[       .C.8...  ]"));
        }

        @Test
        void formatBytesContinuesOnNextLineWhenUnalignedCustomStartOffsetCrossesSixteenByteBoundary() {
            byte[] bytes = "ABCDEFGHIJKL".getBytes(StandardCharsets.US_ASCII);

            String result = SimpleByteFormatter.formatBytes(bytes, 0x00000B0C);

            assertTrue(result.contains("00000B00                                      41 42 43 44"));
            assertTrue(result.contains("[            ABCD]"));
            assertTrue(result.contains("00000B10  45 46 47 48 49 4A 4B 4C"));
            assertTrue(result.contains("[EFGHIJKL        ]"));
        }

        @Test
        void formatBytesRejectsNegativeStartOffset() {
            byte[] bytes = new byte[]{0x01};

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> SimpleByteFormatter.formatBytes(bytes, -1)
            );

            assertEquals("startOffset must not be negative", exception.getMessage());
        }

        @Test
        void formatBytesTerminatesFormattedOutputWithBlankLine() {
            String result = SimpleByteFormatter.formatBytes(new byte[]{0x01});

            assertTrue(result.endsWith("\n\n"));
        }
    }
}