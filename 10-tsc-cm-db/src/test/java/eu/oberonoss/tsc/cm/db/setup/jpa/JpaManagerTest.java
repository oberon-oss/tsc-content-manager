package eu.oberonoss.tsc.cm.db.setup.jpa;

import eu.oberonoss.tsc.cm.db.DatabaseException;
import eu.oberonoss.tsc.cm.db.entity.TscScannedFileEntity;
import eu.oberonoss.tsc.cm.db.entity.TscSettingEntity;
import eu.oberonoss.tsc.cm.db.setup.DatabaseConfig;
import eu.oberonoss.tsc.cm.db.setup.DatabaseManager;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaManagerTest {

    private DatabaseManager databaseManager;
    private JpaManager jpaManager;

    @BeforeEach
    void setUp() {
        DatabaseConfig config = DatabaseConfig.createInMemory("jpa-test-" + System.nanoTime());
        databaseManager = new DatabaseManager(config);
        databaseManager.initialize();
        jpaManager = databaseManager.createJpaManager();
    }

    @AfterEach
    void tearDown() {
        if (jpaManager != null && !jpaManager.isClosed()) {
            jpaManager.close();
        }
        if (databaseManager != null && !databaseManager.isClosed()) {
            databaseManager.shutdown();
        }
    }

    @Test
    void testEntityManagerLifecycleAndClosedCheck() {
        assertThat(jpaManager.isClosed()).isFalse();
        assertThat(jpaManager.getEntityManagerFactory().isOpen()).isTrue();

        EntityManager em = jpaManager.createEntityManager();
        assertThat(em.isOpen()).isTrue();
        em.close();
        assertThat(em.isOpen()).isFalse();

        jpaManager.close();
        assertThat(jpaManager.isClosed()).isTrue();

        assertThatThrownBy(() -> jpaManager.createEntityManager())
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void testTscSettingEntityPersistAndFind() {
        Instant timestamp = Instant.now();
        TscSettingEntity setting = TscSettingEntity.builder()
                .settingKey("general.language")
                .settingValue("en_US")
                .description("Default UI language")
                .updatedAt(timestamp)
                .build();

        jpaManager.executeInTransaction(em -> em.persist(setting));

        TscSettingEntity found = jpaManager.executeInTransactionWithResult(em ->
                em.find(TscSettingEntity.class, "general.language")
        );

        assertThat(found).isNotNull();
        assertThat(found.getSettingKey()).isEqualTo("general.language");
        assertThat(found.getSettingValue()).isEqualTo("en_US");
        assertThat(found.getDescription()).isEqualTo("Default UI language");

        // Update
        jpaManager.executeInTransaction(em -> {
            TscSettingEntity toUpdate = em.find(TscSettingEntity.class, "general.language");
            toUpdate.setSettingValue("de_DE");
        });

        TscSettingEntity updated = jpaManager.executeInTransactionWithResult(em ->
                em.find(TscSettingEntity.class, "general.language")
        );
        assertThat(updated.getSettingValue()).isEqualTo("de_DE");
    }

    @Test
    void testTscScannedFileEntityPersistAndQuery() {
        Instant now = Instant.now();
        TscScannedFileEntity file1 = TscScannedFileEntity.builder()
                .filePath("Assets/Kuju/RailSimulator/RailVehicles/Diesel/Class47.bin")
                .fileName("Class47.bin")
                .fileSize(45000L)
                .fileType("BIN")
                .lastModified(now)
                .scannedAt(now)
                .build();

        TscScannedFileEntity file2 = TscScannedFileEntity.builder()
                .filePath("Assets/DTG/Academy/Environment/Terrain/Textures.xml")
                .fileName("Textures.xml")
                .fileSize(12000L)
                .fileType("XML")
                .lastModified(now)
                .scannedAt(now)
                .build();

        jpaManager.executeInTransaction(em -> {
            em.persist(file1);
            em.persist(file2);
        });

        assertThat(file1.getId()).isNotNull();
        assertThat(file2.getId()).isNotNull();

        List<TscScannedFileEntity> files = jpaManager.executeInTransactionWithResult(em ->
                em.createQuery("SELECT f FROM TscScannedFileEntity f WHERE f.fileType = :type", TscScannedFileEntity.class)
                        .setParameter("type", "BIN")
                        .getResultList()
        );

        assertThat(files).hasSize(1);
        assertThat(files.getFirst().getFileName()).isEqualTo("Class47.bin");
    }

    @Test
    void testTransactionRollbackOnException() {
        TscSettingEntity setting = TscSettingEntity.builder()
                .settingKey("rollback.test")
                .settingValue("initial")
                .build();

        jpaManager.executeInTransaction(em -> em.persist(setting));

        assertThatThrownBy(() ->
                jpaManager.executeInTransaction(em -> {
                    TscSettingEntity loaded = em.find(TscSettingEntity.class, "rollback.test");
                    loaded.setSettingValue("should_not_persist");
                    throw new RuntimeException("Simulated error");
                })
        ).isInstanceOf(DatabaseException.class);

        TscSettingEntity afterRollback = jpaManager.executeInTransactionWithResult(em ->
                em.find(TscSettingEntity.class, "rollback.test")
        );
        assertThat(afterRollback.getSettingValue()).isEqualTo("initial");
    }

    @Test
    void testCreateFromDatabaseConfig(@TempDir Path tempDir) {
        DatabaseConfig config = DatabaseConfig.createInDirectory(tempDir, "config-jpa-" + System.nanoTime());
        // Initialize migrations first
        try (DatabaseManager dbm = new DatabaseManager(config)) {
            dbm.initialize();
        }

        try (JpaManager customJpa = JpaManager.create(config)) {
            assertThat(customJpa.isClosed()).isFalse();
            TscSettingEntity setting = customJpa.executeInTransactionWithResult(em -> {
                TscSettingEntity entity = TscSettingEntity.builder()
                        .settingKey("config.test")
                        .settingValue("val")
                        .build();
                em.persist(entity);
                return entity;
            });
            assertThat(setting.getSettingKey()).isEqualTo("config.test");
        }
    }

    @Test
    void testInvalidPersistenceUnitThrowsDatabaseException() {
        DataSource source = databaseManager.getDataSource();
        assertThatThrownBy(() -> JpaManager.create("invalid-pu", source))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Failed to create EntityManagerFactory");
    }

    @Test
    void testCreateFromRemoteDatabaseConfig() {
        DatabaseConfig remoteConfig = DatabaseConfig.builder()
                .host("localhost")
                .port(1527)
                .dbName("remote-jpa-test")
                .build();

        assertThatThrownBy(() -> JpaManager.create(remoteConfig))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Failed to create EntityManagerFactory");
    }
}
