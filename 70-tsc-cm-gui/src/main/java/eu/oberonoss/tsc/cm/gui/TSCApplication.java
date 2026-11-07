package eu.oberonoss.tsc.cm.gui;

import eu.oberonoss.tsc.cm.gui.components.TSCApplicationWindow;
import eu.oberonoss.tsc.cm.shared.i18n.I18n;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * Runs the interactive TSC application
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Slf4j
public class TSCApplication {
    @Getter
    private final TSCApplicationWindow window;

    public TSCApplication() {
        window = new TSCApplicationWindow();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setTitle(I18n.get("gui.title"));
    }

    /**
     * Entry point for running the TSC application GUI.
     *
     * @param args Command line arguments.
     *
     * @since 1.0.0
     */
    public static void main(String[] args) {
        LOGGER.info("Starting TSC Application GUI...");
        SwingUtilities.invokeLater(() -> {
            TSCApplication app = new TSCApplication();
            app.getWindow().setVisible(true);
        });
    }
}
