package eu.oberonoss.tsc.cm.shared.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class I18nTest {

    private Locale originalDefaultLocale;

    @BeforeEach
    void setUp() {
        originalDefaultLocale = Locale.getDefault();
        I18n.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(originalDefaultLocale);
        I18n.resetLocale();
    }

    @Test
    void testGetLocaleAndSetLocale() {
        I18n.setLocale(Locale.GERMAN);
        assertEquals(Locale.GERMAN, I18n.getLocale());

        I18n.setLocale(null);
        assertEquals(Locale.getDefault(), I18n.getLocale());

        I18n.resetLocale();
        assertEquals(Locale.getDefault(), I18n.getLocale());
    }

    @Test
    void testGetBundle() {
        ResourceBundle bundleDefault = I18n.getBundle();
        assertNotNull(bundleDefault);
        assertTrue(bundleDefault.containsKey("app.name"));

        ResourceBundle bundleGerman = I18n.getBundle(Locale.GERMAN);
        assertNotNull(bundleGerman);
        assertEquals("TSC Content Manager", bundleGerman.getString("app.name"));
        assertEquals("Abbrechen", bundleGerman.getString("common.cancel"));
    }

    @Test
    void testContainsKey() {
        assertTrue(I18n.containsKey("app.name"));
        assertTrue(I18n.containsKey("common.ok"));
        assertFalse(I18n.containsKey("non.existing.key"));
        assertFalse(I18n.containsKey(null));

        assertTrue(I18n.containsKey(Locale.GERMAN, "common.cancel"));
        assertFalse(I18n.containsKey(Locale.GERMAN, "non.existing.key"));
        assertFalse(I18n.containsKey(Locale.GERMAN, null));
    }

    @Test
    void testGet() {
        I18n.setLocale(Locale.ENGLISH);
        assertEquals("TSC Content Manager", I18n.get("app.name"));
        assertEquals("Cancel", I18n.get("common.cancel"));

        I18n.setLocale(Locale.GERMAN);
        assertEquals("Abbrechen", I18n.get("common.cancel"));

        I18n.setLocale(Locale.FRENCH);
        assertEquals("Annuler", I18n.get("common.cancel"));
    }

    @Test
    void testGetWithSpecificLocale() {
        assertEquals("Cancel", I18n.get(Locale.ENGLISH, "common.cancel"));
        assertEquals("Abbrechen", I18n.get(Locale.GERMAN, "common.cancel"));
        assertEquals("Annuler", I18n.get(Locale.FRENCH, "common.cancel"));
    }

    @Test
    void testGetMissingAndNullKey() {
        assertEquals("!non.existent.key!", I18n.get("non.existent.key"));
        assertEquals("!non.existent.key!", I18n.get(Locale.GERMAN, "non.existent.key"));
        assertEquals("!null!", I18n.get(null));
        assertEquals("!null!", I18n.get(Locale.GERMAN, null));
    }

    @Test
    void testFormat() {
        I18n.setLocale(Locale.ENGLISH);
        assertEquals("Version 1.0.0", I18n.format("app.version", "1.0.0"));

        assertEquals("Version 2.5.0", I18n.format(Locale.GERMAN, "app.version", "2.5.0"));
        assertEquals("Version 3.0.0", I18n.format(Locale.FRENCH, "app.version", "3.0.0"));
    }

    @Test
    void testFormatWithoutArgsOrNullArgs() {
        I18n.setLocale(Locale.ENGLISH);
        assertEquals("Version {0}", I18n.format("app.version"));
        assertEquals("Version {0}", I18n.format("app.version", (Object[]) null));
        assertEquals("Version {0}", I18n.format(Locale.GERMAN, "app.version", (Object[]) null));
    }

    @Test
    void testFormatWithMissingKey() {
        assertEquals("!missing.key!", I18n.format("missing.key", "arg1", "arg2"));
        assertEquals("!missing.key!", I18n.format(Locale.GERMAN, "missing.key", "arg1"));
    }
}
