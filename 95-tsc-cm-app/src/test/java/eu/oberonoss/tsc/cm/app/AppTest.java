package eu.oberonoss.tsc.cm.app;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test for {@link App}.
 */
class AppTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);

    /**
     * Tests that creating an instance of {@link App} does not throw an exception and that the resulting object is not null.
     */
    @Test
    void shouldCreateAppInstance() {
        assertDoesNotThrow(() -> {
            App app = new App();
            assertNotNull(app);
            LOGGER.info("App instance created: {}", app);
        });
    }
}
