package eu.oberonoss.tsc.cm.gui;

import eu.oberonoss.tsc.cm.shared.i18n.I18n;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class I18nGuiAccessibilityTest {

    @Test
    void testI18nAccessibleInGuiModule() {
        assertEquals("TSC Content Manager", I18n.get(Locale.ENGLISH, "app.name"));
        assertEquals("OK", I18n.get(Locale.ENGLISH, "common.ok"));
        assertEquals("Abbrechen", I18n.get(Locale.GERMAN, "common.cancel"));
        assertEquals("Annuler", I18n.get(Locale.FRENCH, "common.cancel"));
    }

    @Test
    void testResourceBundleDirectLookupInGuiModule() {
        ResourceBundle bundle = ResourceBundle.getBundle(I18n.BUNDLE_BASE_NAME, Locale.GERMAN);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey("gui.title"));
        assertEquals("TSC Content Manager", bundle.getString("gui.title"));
        assertEquals("Inhalt scannen", bundle.getString("gui.button.scan"));
    }
}
