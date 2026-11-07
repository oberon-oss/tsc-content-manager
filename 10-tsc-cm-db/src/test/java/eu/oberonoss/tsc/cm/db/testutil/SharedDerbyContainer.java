package eu.oberonoss.tsc.cm.db.testutil;

import eu.oberonoss.tsc.cm.db.setup.DatabaseConfig;
import org.apache.derby.jdbc.ClientDataSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.time.Duration;

/**
 * Shared singleton Derby Testcontainer instance for fast, reusable multi-module test execution.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class SharedDerbyContainer {

    /**
     * Standard Apache Derby Network Server port.
     */
    public static final int DERBY_PORT = 1527;

    /**
     * Default Docker image for Derby Network Server.
     */
    public static final String DEFAULT_IMAGE = "az82/docker-derby:latest";

    /**
     * Default database name for shared test executions.
     */
    public static final String SHARED_DB_NAME = "tsc_test_db";

    private static volatile GenericContainer<?> container;

    static {
        try {
            java.nio.file.Path propFile = java.nio.file.Path.of(System.getProperty("user.home"), ".testcontainers.properties");
            if (!java.nio.file.Files.exists(propFile)) {
                java.nio.file.Files.writeString(propFile, "testcontainers.reuse.enable=true\n");
            }
        } catch (Exception _) {
            // Fall back gracefully if environment permissions restrict writing to user home
        }
    }

    private SharedDerbyContainer() {
    }

    /**
     * Retrieves or initializes the singleton Testcontainers GenericContainer instance running Derby.
     *
     * @return The running GenericContainer instance.
     */
    public static GenericContainer<?> getInstance() {
        if (container == null) {
            synchronized (SharedDerbyContainer.class) {
                if (container == null) {
                    String imageName = System.getProperty("testcontainers.derby.image", DEFAULT_IMAGE);
                    //noinspection resource
                    GenericContainer<?> c = new GenericContainer<>(DockerImageName.parse(imageName))
                            .withExposedPorts(DERBY_PORT)
                            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
                            .withReuse(true);
                    c.start();
                    waitForDerbyReadiness(c);
                    container = c;
                }
            }
        }
        return container;
    }

    private static void waitForDerbyReadiness(GenericContainer<?> c) {
        String host = c.getHost();
        int port = c.getMappedPort(DERBY_PORT);
        long deadline = System.currentTimeMillis() + 30_000;
        Exception lastException = null;

        while (System.currentTimeMillis() < deadline) {
            try {
                ClientDataSource ds = new ClientDataSource();
                ds.setServerName(host);
                ds.setPortNumber(port);
                ds.setDatabaseName("derby_readiness_probe");
                ds.setCreateDatabase("create");
                try (Connection conn = ds.getConnection()) {
                    if (conn != null && !conn.isClosed()) {
                        return;
                    }
                }
            } catch (Exception e) {
                lastException = e;
                try {
                    //noinspection BusyWait - May be replaced with Awaitability library if more sleeps are needed in the application
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for Derby container readiness", ie);
                }
            }
        }
        throw new RuntimeException("Derby container at " + host + ":" + port + " failed readiness check", lastException);
    }

    /**
     * Retrieves the host address where the container is mapped.
     *
     * @return The container host.
     */
    public static String getHost() {
        return getInstance().getHost();
    }

    /**
     * Retrieves the mapped port on the host machine for the Derby Network Server.
     *
     * @return The mapped port integer.
     */
    public static int getPort() {
        return getInstance().getMappedPort(DERBY_PORT);
    }

    /**
     * Constructs a JDBC URL pointing to the running container instance for the specified database name.
     *
     * @param dbName The database name.
     *
     * @return The remote JDBC URL.
     */
    public static String getJdbcUrl(String dbName) {
        return String.format("jdbc:derby://%s:%d/%s;create=true",
                getHost(),
                getPort(),
                dbName);
    }

    /**
     * Creates a {@link DatabaseConfig} targeting the running container for the given database name.
     *
     * @param dbName The database name.
     *
     * @return The configured {@link DatabaseConfig}.
     */
    public static DatabaseConfig createDatabaseConfig(String dbName) {
        return DatabaseConfig.builder()
                .host(getHost())
                .port(getPort())
                .dbName(dbName)
                .autoMigrate(true)
                .build();
    }

    /**
     * Creates a {@link DatabaseConfig} targeting the shared database instance {@value #SHARED_DB_NAME}.
     *
     * @return The configured {@link DatabaseConfig}.
     */
    public static DatabaseConfig createSharedDatabaseConfig() {
        return createDatabaseConfig(SHARED_DB_NAME);
    }
}
