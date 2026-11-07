package eu.oberonoss.tsc.cm.db.setup;

import eu.oberonoss.tsc.cm.db.entity.TscScannedFileEntity;
import eu.oberonoss.tsc.cm.db.entity.TscSettingEntity;
import eu.oberonoss.tsc.cm.db.testutil.AbstractDatabaseTest;
import eu.oberonoss.tsc.cm.db.testutil.SharedDerbyContainer;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SharedDerbyContainerIntegrationTest extends AbstractDatabaseTest {

    @Test
    void testSharedContainerIsRunningAndAccessible() throws Exception {
        assumeTrue(SharedDerbyContainer.isAvailable(), "Derby Testcontainer is not available in the current environment");
        assertThat(SharedDerbyContainer.getInstance().isRunning()).isTrue();
        assertThat(SharedDerbyContainer.getHost()).isNotNull();
        assertThat(SharedDerbyContainer.getPort()).isPositive();

        //noinspection SqlNoDataSourceInspection
        try (Connection conn = sharedDatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM DATABASECHANGELOG")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void testJpaOperationsAgainstSharedContainer() {
        Instant now = Instant.now();
        TscSettingEntity setting = TscSettingEntity.builder()
                .settingKey("test.remote.key")
                .settingValue("remote-value")
                .description("Remote Test")
                .updatedAt(now)
                .build();

        sharedJpaManager.executeInTransaction(em -> em.persist(setting));

        TscSettingEntity found = sharedJpaManager.executeInTransactionWithResult(em ->
                em.find(TscSettingEntity.class, "test.remote.key"));

        assertThat(found).isNotNull();
        assertThat(found.getSettingValue()).isEqualTo("remote-value");

        TscScannedFileEntity file = TscScannedFileEntity.builder()
                .filePath("Assets/Test/Remote")
                .fileName("test.bin")
                .fileSize(2048L)
                .fileType("bin")
                .lastModified(now)
                .scannedAt(now)
                .build();

        sharedJpaManager.executeInTransaction(em -> em.persist(file));

        List<TscScannedFileEntity> files = sharedJpaManager.executeInTransactionWithResult(em ->
                em.createQuery("SELECT f FROM TscScannedFileEntity f WHERE f.fileName = :name", TscScannedFileEntity.class)
                        .setParameter("name", "test.bin")
                        .getResultList());

        assertThat(files).hasSize(1);
        assertThat(files.getFirst().getFileSize()).isEqualTo(2048L);
    }

    @Test
    void testCreateDatabaseConfigProducesValidConfig() {
        var config = SharedDerbyContainer.createDatabaseConfig("test_db_config");
        assertThat(config).isNotNull();
        assertThat(config.getDbName()).isEqualTo("test_db_config");
        assertThat(config.isAutoMigrate()).isTrue();
        if (SharedDerbyContainer.isAvailable()) {
            assertThat(config.isRemote()).isTrue();
            assertThat(config.getHost()).isEqualTo(SharedDerbyContainer.getHost());
            assertThat(config.getPort()).isEqualTo(SharedDerbyContainer.getPort());
        } else {
            assertThat(config.isInMemory()).isTrue();
        }

        String jdbcUrl = SharedDerbyContainer.getJdbcUrl("test_db_config");
        assertThat(jdbcUrl).isNotBlank();
        if (SharedDerbyContainer.isAvailable()) {
            assertThat(jdbcUrl).startsWith("jdbc:derby://");
        } else {
            assertThat(jdbcUrl).startsWith("jdbc:derby:memory:");
        }
    }
}
