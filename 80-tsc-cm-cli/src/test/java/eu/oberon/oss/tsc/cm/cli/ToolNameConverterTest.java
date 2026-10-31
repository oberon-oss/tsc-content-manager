package eu.oberon.oss.tsc.cm.cli;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.Command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolNameConverterTest {

    @BeforeEach
    @AfterEach
    void cleanUp() {
        ToolNameConverter.reset();
    }

    @Command(name = "custom-tool")
    static class AnnotatedTool {
    }

    static class UnannotatedTool {
    }

    @Test
    void testSetToolNameString() {
        ToolNameConverter.setToolName("my-tool");
        assertEquals("my-tool", ToolNameConverter.getToolName());

        ToolNameConverter converter = new ToolNameConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        assertEquals("my-tool", converter.convert(event));
    }

    @Test
    void testSetToolNameFromAnnotatedClass() {
        ToolNameConverter.setToolName(AnnotatedTool.class);
        assertEquals("custom-tool", ToolNameConverter.getToolName());
    }

    @Test
    void testSetToolNameFromUnannotatedClass() {
        ToolNameConverter.setToolName(UnannotatedTool.class);
        assertEquals("UnannotatedTool", ToolNameConverter.getToolName());
    }

    @Test
    void testSetToolNameNullClass() {
        ToolNameConverter.setToolName("my-tool");
        ToolNameConverter.setToolName((Class<?>) null);
        assertNull(ToolNameConverter.getToolName());
    }

    @Test
    void testFallbackWhenNoToolNameSet() {
        ToolNameConverter converter = new ToolNameConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getLoggerName()).thenReturn("eu.oberonoss.tsc.cm.io.util.FileScannerImpl");

        assertEquals("FileScannerImpl", converter.convert(event));
    }

    @Test
    void testFallbackWhenLoggerNameHasNoPackage() {
        ToolNameConverter converter = new ToolNameConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getLoggerName()).thenReturn("SimpleLogger");

        assertEquals("SimpleLogger", converter.convert(event));
    }

    @Test
    void testFallbackWhenEventOrLoggerNameNull() {
        ToolNameConverter converter = new ToolNameConverter();
        assertEquals("tsc-tool", converter.convert(null));

        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getLoggerName()).thenReturn(null);
        assertEquals("tsc-tool", converter.convert(event));
    }
}
