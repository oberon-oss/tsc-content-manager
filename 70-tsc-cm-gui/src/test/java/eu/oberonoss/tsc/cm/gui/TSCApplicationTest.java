package eu.oberonoss.tsc.cm.gui;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TSCApplicationTest {

    @Test
    void testInstantiation() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        TSCApplication app = new TSCApplication();
        assertNotNull(app);
        assertNotNull(app.getWindow());
        assertEquals(WindowConstants.EXIT_ON_CLOSE, app.getWindow().getDefaultCloseOperation());
    }

    @Test
    void testMainMethod() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        assertDoesNotThrow(TSCApp lication::main);
    }
}
