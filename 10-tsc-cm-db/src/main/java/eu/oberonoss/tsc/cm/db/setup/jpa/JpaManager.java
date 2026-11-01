package eu.oberonoss.tsc.cm.db.setup.jpa;

import eu.oberonoss.tsc.cm.db.DatabaseException;
import eu.oberonoss.tsc.cm.db.setup.DatabaseConfig;
import eu.oberonoss.tsc.cm.db.setup.DatabaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.hibernate.cfg.JdbcSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manages JPA / Hibernate EntityManagerFactory lifecycle and transactional execution.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class JpaManager implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(JpaManager.class);

    /**
     * Default persistence unit name defined in persistence.xml.
     *
     * @since 1.0.0
     */
    public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "tsc-cm-pu";

    private final EntityManagerFactory entityManagerFactory;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Constructs a JpaManager with an existing EntityManagerFactory.
     *
     * @param entityManagerFactory The EntityManagerFactory instance.
     *
     * @since 1.0.0
     */
    public JpaManager(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory, "entityManagerFactory must not be null");
    }

    /**
     * Creates a JpaManager bound to the DataSource from a DatabaseManager instance.
     *
     * @param databaseManager The DatabaseManager instance.
     *
     * @return A configured JpaManager.
     *
     * @since 1.0.0
     */
    public static JpaManager create(DatabaseManager databaseManager) {
        Objects.requireNonNull(databaseManager, "databaseManager must not be null");
        if (!databaseManager.isInitialized()) {
            databaseManager.initialize();
        }
        return create(databaseManager.getDataSource());
    }

    /**
     * Creates a JpaManager bound to the specified DataSource.
     *
     * @param dataSource The DataSource.
     *
     * @return A configured JpaManager.
     *
     * @since 1.0.0
     */
    public static JpaManager create(DataSource dataSource) {
        return create(DEFAULT_PERSISTENCE_UNIT_NAME, dataSource);
    }

    /**
     * Creates a JpaManager bound to the specified DataSource and persistence unit name.
     *
     * @param persistenceUnitName The persistence unit name.
     * @param dataSource          The DataSource.
     *
     * @return A configured JpaManager.
     *
     * @since 1.0.0
     */
    public static JpaManager create(String persistenceUnitName, DataSource dataSource) {
        Objects.requireNonNull(persistenceUnitName, "persistenceUnitName must not be null");
        Objects.requireNonNull(dataSource, "dataSource must not be null");

        Map<String, Object> properties = new HashMap<>();
        properties.put(JdbcSettings.JAKARTA_NON_JTA_DATASOURCE, dataSource);

        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
            return new JpaManager(emf);
        } catch (Exception e) {
            log.error("Failed to create EntityManagerFactory for persistence unit '{}'", persistenceUnitName, e);
            throw new DatabaseException("Failed to create EntityManagerFactory: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a JpaManager configured with the specified DatabaseConfig.
     *
     * @param config The DatabaseConfig.
     *
     * @return A configured JpaManager.
     *
     * @since 1.0.0
     */
    public static JpaManager create(DatabaseConfig config) {
        return create(DEFAULT_PERSISTENCE_UNIT_NAME, config);
    }

    /**
     * Creates a JpaManager configured with the specified persistence unit name and DatabaseConfig.
     *
     * @param persistenceUnitName The persistence unit name.
     * @param config              The DatabaseConfig.
     *
     * @return A configured JpaManager.
     *
     * @since 1.0.0
     */
    public static JpaManager create(String persistenceUnitName, DatabaseConfig config) {
        Objects.requireNonNull(persistenceUnitName, "persistenceUnitName must not be null");
        Objects.requireNonNull(config, "config must not be null");

        Map<String, Object> properties = new HashMap<>();
        properties.put(JdbcSettings.JAKARTA_JDBC_URL, config.getJdbcUrl());
        properties.put(JdbcSettings.JAKARTA_JDBC_DRIVER, "org.apache.derby.jdbc.EmbeddedDriver");

        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
            return new JpaManager(emf);
        } catch (Exception e) {
            log.error("Failed to create EntityManagerFactory for persistence unit '{}'", persistenceUnitName, e);
            throw new DatabaseException("Failed to create EntityManagerFactory: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the underlying EntityManagerFactory.
     *
     * @return The EntityManagerFactory.
     *
     * @since 1.0.0
     */
    public EntityManagerFactory getEntityManagerFactory() {
        ensureOpen();
        return entityManagerFactory;
    }

    /**
     * Creates a new EntityManager.
     *
     * @return A new EntityManager instance.
     *
     * @since 1.0.0
     */
    public EntityManager createEntityManager() {
        ensureOpen();
        return entityManagerFactory.createEntityManager();
    }

    /**
     * Executes an action within a database transaction.
     *
     * @param action The consumer action to execute.
     *
     * @throws DatabaseException If transaction execution fails.
     * @since 1.0.0
     */
    public void executeInTransaction(Consumer<EntityManager> action) {
        Objects.requireNonNull(action, "action must not be null");
        executeInTransactionWithResult(em -> {
            action.accept(em);
            return null;
        });
    }

    /**
     * Executes a function within a database transaction and returns the result.
     *
     * @param <T>    The return type.
     * @param action The function to execute.
     *
     * @return The result returned by the function.
     *
     * @throws DatabaseException If transaction execution fails.
     * @since 1.0.0
     */
    public <T> T executeInTransactionWithResult(Function<EntityManager, T> action) {
        Objects.requireNonNull(action, "action must not be null");
        ensureOpen();

        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = action.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    log.error("Failed to rollback transaction", rbEx);
                }
            }
            log.error("Transaction execution failed", e);
            throw new DatabaseException("Transaction execution failed: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Checks whether the JpaManager is closed.
     *
     * @return True if closed, false otherwise.
     *
     * @since 1.0.0
     */
    public boolean isClosed() {
        return closed.get() || !entityManagerFactory.isOpen();
    }

    /**
     * Closes the EntityManagerFactory.
     *
     * @since 1.0.0
     */
    @Override
    public synchronized void close() {
        if (closed.compareAndSet(false, true) && entityManagerFactory.isOpen()) {
            log.info("Closing JPA EntityManagerFactory.");
            entityManagerFactory.close();
        }
    }

    private void ensureOpen() {
        if (isClosed()) {
            throw new DatabaseException("JpaManager is closed.");
        }
    }
}
