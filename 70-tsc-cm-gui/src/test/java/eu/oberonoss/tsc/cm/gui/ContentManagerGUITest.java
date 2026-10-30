package eu.oberonoss.tsc.cm.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContentManagerGUITest {

    @Test
    void testInstantiation() {
        ContentManagerGUI gui = new ContentManagerGUI();
        assertNotNull(gui);
    }
}
