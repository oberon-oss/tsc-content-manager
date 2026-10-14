package eu.oberonoss.tsc.cm.io.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SerzFileTypeDetectorTest {

    public static Stream<Arguments> testSerzFileTypeDetectorForValidFiles() {
        return Stream.of(
                Arguments.of(Path.of("src/test/resources/util/serzdetector/01-valid.bin"), SerzFileTypeDetector.SERZ_BINARY_CONTENT_TYPE),
                Arguments.of(Path.of("src/test/resources/util/serzdetector/01-valid.xml"), SerzFileTypeDetector.SERZ_XML_CONTENT_TYPE),
                Arguments.of(Path.of("src/test/resources/util/serzdetector/03-valid-delta.xml"), SerzFileTypeDetector.SERZ_XML_CONTENT_TYPE)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testSerzFileTypeDetectorForValidFiles(Path path, String expectedContentType) {
        String contentType = assertDoesNotThrow(() -> Files.probeContentType(path));
        assertEquals(expectedContentType, contentType);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/resources/util/serzdetector/02-invalid.bin",
            "src/test/resources/util/serzdetector/04-invalid.xml"}
    )
    void testSerzFileTypeDetectorForInvalidFiles(String path) {
        SerzFileTypeDetector detector = new SerzFileTypeDetector();
        String contentType = assertDoesNotThrow(() -> detector.probeContentType(Path.of(path)));
        assertNull(contentType);
    }

    @Test
    void testIsSerzFileBinary() {
        byte[] validHeader = new byte[]{0x53, 0x45, 0x52, 0x5A, 0x00, 0x00, 0x01, 0x00};
        byte[] invalidHeader = new byte[]{0x53, 0x45, 0x52, 0x5A, 0x00, 0x00, 0x00, 0x00};

        assertTrue(SerzFileTypeDetector.isValidBinarySerzHeader(validHeader));
        assertFalse(SerzFileTypeDetector.isValidBinarySerzHeader(invalidHeader));
        assertFalse(SerzFileTypeDetector.isValidBinarySerzHeader(null));
        assertFalse(SerzFileTypeDetector.isValidBinarySerzHeader(new byte[]{0x53, 0x45}));
    }

    public static Stream<Arguments> provideXmlTestCases() {
        return Stream.of(
                Arguments.of(
                        SerzFileTypeDetector.XML_HEADER + "\n" + SerzFileTypeDetector.BLUEPRINT_LOADER_HEADER,
                        SerzFileTypeDetector.SERZ_XML_CONTENT_TYPE
                ),
                Arguments.of(
                        SerzFileTypeDetector.XML_HEADER + "\r\n" + SerzFileTypeDetector.DELTA_LOADER_HEADER,
                        SerzFileTypeDetector.SERZ_XML_CONTENT_TYPE
                ),
                Arguments.of(
                        SerzFileTypeDetector.XML_HEADER + SerzFileTypeDetector.BLUEPRINT_LOADER_HEADER,
                        SerzFileTypeDetector.SERZ_XML_CONTENT_TYPE
                ),
                Arguments.of(
                        SerzFileTypeDetector.XML_HEADER + "  " + SerzFileTypeDetector.DELTA_LOADER_HEADER,
                        SerzFileTypeDetector.SERZ_XML_CONTENT_TYPE
                ),
                Arguments.of(
                        "\uFEFF" + SerzFileTypeDetector.XML_HEADER + "\n" + SerzFileTypeDetector.BLUEPRINT_LOADER_HEADER,
                        SerzFileTypeDetector.SERZ_XML_CONTENT_TYPE
                ),
                Arguments.of(
                        "\uFEFF" + SerzFileTypeDetector.XML_HEADER + SerzFileTypeDetector.DELTA_LOADER_HEADER,
                        SerzFileTypeDetector.SERZ_XML_CONTENT_TYPE
                ),
                Arguments.of(
                        SerzFileTypeDetector.XML_HEADER,
                        null
                ),
                Arguments.of(
                        "",
                        null
                ),
                Arguments.of(
                        SerzFileTypeDetector.XML_HEADER + "\n<otherRootElement>",
                        null
                ),
                Arguments.of(
                        "<?xml version=\"1.0\"?>\n" + SerzFileTypeDetector.BLUEPRINT_LOADER_HEADER,
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideXmlTestCases")
    void testProbeContentTypeXmlVariants(String fileContent, String expectedContentType) throws IOException {
        Path tempFile = Files.createTempFile("serz-detector-test-", ".xml");
        try {
            Files.writeString(tempFile, fileContent, StandardCharsets.UTF_8);

            SerzFileTypeDetector detector = new SerzFileTypeDetector();
            String actualContentType = detector.probeContentType(tempFile);

            assertEquals(expectedContentType, actualContentType);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}