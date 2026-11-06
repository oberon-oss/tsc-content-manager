package eu.oberon.oss.tsc.cm.cli;

import eu.oberonoss.tsc.cm.shared.i18n.I18n;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class I18nCliAccessibilityTest {

    @Test
    void testI18nAccessibleInCliModule() {
        assertEquals("TSC Content Manager", I18n.get(Locale.ENGLISH, "app.name"));
        assertEquals("Directory where the database files reside", I18n.get(Locale.ENGLISH, "cli.option.db.directory"));
        assertEquals("Verzeichnis, in dem sich die Datenbankdateien befinden", I18n.get(Locale.GERMAN, "cli.option.db.directory"));
        assertEquals("Répertoire où résident les fichiers de la base de données", I18n.get(Locale.FRENCH, "cli.option.db.directory"));
    }

    @Test
    void testResourceBundleDirectLookupInCliModule() {
        ResourceBundle bundle = ResourceBundle.getBundle(I18n.BUNDLE_BASE_NAME, Locale.ENGLISH);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey("cli.migration.start"));
        assertEquals("Starting Liquibase database migration process...", bundle.getString("cli.migration.start"));
    }
}
