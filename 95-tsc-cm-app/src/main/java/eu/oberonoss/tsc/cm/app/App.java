package eu.oberonoss.tsc.cm.app;

import lombok.extern.slf4j.Slf4j;

/**
 * Entry point application class.
 */
@Slf4j
public class App {

    /**
     * Default constructor.
     */
    public App() {
        LOGGER.info("Application started");
    }

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
