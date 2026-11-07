package eu.oberonoss.tsc.cm.db.testutil;

import eu.oberonoss.tsc.cm.db.setup.DatabaseConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

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
                    GenericContainer<?> c = new GenericContainer<>(DockerImageName.parse(imageName))
                            .withExposedPorts(DERBY_PORT)
                            .waitingFor(Wait.forListeningPort())
                            .withReuse(true);
                    c.start();
                    container = c;
                }
            }
        }
        return container;
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
