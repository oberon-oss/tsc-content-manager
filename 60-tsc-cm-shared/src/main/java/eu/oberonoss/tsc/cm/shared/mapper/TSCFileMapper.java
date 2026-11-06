package eu.oberonoss.tsc.cm.shared.mapper;

import eu.oberonoss.tsc.cm.base.cfg.BaseConfiguration;
import eu.oberonoss.tsc.cm.db.entity.TscScannedFileEntity;
import eu.oberonoss.tsc.cm.db.setup.DatabaseConfig;
import eu.oberonoss.tsc.cm.db.setup.DatabaseManager;
import eu.oberonoss.tsc.cm.db.setup.jpa.JpaManager;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.HibernateHints;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * A singleton mapper that maps data from {@link TscScannedFileEntity} records to actual files on the file system.
 * <p>
 * Connects records from the database table to real file paths by combining the installation base directory resolved via
 * {@link BaseConfiguration#resolveDefaultInstallationDirectory()} with the record's file path and file name.
 * </p>
 * <p>
 * Data is loaded once at initialization time from the database. The internal mapping is immutable to users and is not directly accessible from outside the
 * class.
 * </p>
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Slf4j
public final class TSCFileMapper {

    private static final TSCFileMapper INSTANCE = new TSCFileMapper();

    private final ConcurrentHashMap<Path, ScannedFileInfo> fileMap = new ConcurrentHashMap<>();

    /**
     * Private constructor to enforce the singleton pattern and load data at init time.
     */
    private TSCFileMapper() {
        loadData();
    }

    /**
     * Retrieves the singleton instance of {@code TSCFileMapper}.
     *
     * @return The singleton instance.
     *
     * @since 1.0.0
     */
    public static TSCFileMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Loads scanned file records from the database into the mapper cache at initialization time.
     */
    private void loadData() {
        LOGGER.info("Loading scanned file records from database into mapper cache during initialization...");
        try {
            DatabaseConfig config = DatabaseConfig.createDefault();
            try (DatabaseManager dbManager = new DatabaseManager(config)) {
                dbManager.initialize();
                try (JpaManager jpaManager = JpaManager.create(dbManager);
                     EntityManager em = jpaManager.createEntityManager()) {
                    loadFromEntityManager(em);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load scanned files during initialization: {}", e.getMessage(), e);
        }
    }

    /**
     * Resource-efficiently queries scanned file records using JPA streaming and read-only hints.
     *
     * @param entityManager The EntityManager to query with.
     */
    private void loadFromEntityManager(EntityManager entityManager) {
        if (entityManager == null) {
            return;
        }
        try (Stream<TscScannedFileEntity> stream = entityManager
                .createQuery("SELECT e FROM TscScannedFileEntity e", TscScannedFileEntity.class)
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .setHint(HibernateHints.HINT_FETCH_SIZE, 1000)
                .getResultStream()) {
            stream.forEach(entity -> {
                if (entity != null) {
                    Path resolvedPath = resolvePath(entity);
                    ScannedFileInfo fileInfo = ScannedFileInfo.fromEntity(entity);
                    fileMap.put(resolvedPath, fileInfo);
                }
            });
            LOGGER.info("Loaded {} scanned file entries into mapper cache", fileMap.size());
        } catch (Exception e) {
            LOGGER.warn("Could not query scanned files from database: {}", e.getMessage(), e);
        }
    }

    /**
     * Resolves the full file system {@link Path} for a given {@link TscScannedFileEntity} by combining the base installation directory with the entity's file
     * path and file name.
     *
     * @param entity The scanned file entity.
     *
     * @return The resolved normalized file system path, or {@code null} if entity is null.
     *
     * @since 1.0.0
     */
    public Path resolvePath(TscScannedFileEntity entity) {
        if (entity == null) {
            return null;
        }
        return resolvePath(entity.getFilePath(), entity.getFileName());
    }

    /**
     * Resolves the full file system {@link Path} for a relative file path and file name against the default installation directory.
     *
     * @param filePath The relative directory path.
     * @param fileName The file name.
     *
     * @return The resolved normalized file system path.
     *
     * @since 1.0.0
     */
    public Path resolvePath(String filePath, String fileName) {
        Path baseDir = BaseConfiguration.resolveDefaultInstallationDirectory();
        Path path = baseDir != null ? baseDir : Paths.get("");
        if (filePath != null && !filePath.isBlank()) {
            String sanitizedFilePath = filePath.replaceFirst("^[\\\\/]+", "");
            if (!sanitizedFilePath.isBlank()) {
                path = path.resolve(sanitizedFilePath);
            }
        }
        if (fileName != null && !fileName.isBlank()) {
            path = path.resolve(fileName);
        }
        return path.normalize();
    }

    /**
     * Retrieves the {@link ScannedFileInfo} for the given file path.
     *
     * @param path The file path.
     *
     * @return The {@link ScannedFileInfo}, or {@code null} if not found.
     *
     * @since 1.0.0
     */
    public ScannedFileInfo get(Path path) {
        if (path == null) {
            return null;
        }
        return fileMap.get(path.normalize());
    }

    /**
     * Retrieves the {@link ScannedFileInfo} for the given {@link File}.
     *
     * @param file The file.
     *
     * @return The {@link ScannedFileInfo}, or {@code null} if not found.
     *
     * @since 1.0.0
     */
    public ScannedFileInfo get(File file) {
        if (file == null) {
            return null;
        }
        return get(file.toPath());
    }

    /**
     * Retrieves the {@link ScannedFileInfo} for the given relative file path and file name.
     *
     * @param filePath The relative directory path.
     * @param fileName The file name.
     *
     * @return The {@link ScannedFileInfo}, or {@code null} if not found.
     *
     * @since 1.0.0
     */
    public ScannedFileInfo get(String filePath, String fileName) {
        return get(resolvePath(filePath, fileName));
    }

    /**
     * Checks if the mapping contains an entry for the given file path.
     *
     * @param path The file path.
     *
     * @return {@code true} if present, {@code false} otherwise.
     *
     * @since 1.0.0
     */
    public boolean containsKey(Path path) {
        return path != null && fileMap.containsKey(path.normalize());
    }

    /**
     * Checks if the mapping contains an entry for the given {@link File}.
     *
     * @param file The file.
     *
     * @return {@code true} if present, {@code false} otherwise.
     *
     * @since 1.0.0
     */
    public boolean containsKey(File file) {
        return file != null && containsKey(file.toPath());
    }

    /**
     * Checks if the mapping contains an entry for the given file path.
     *
     * @param path The file path.
     *
     * @return {@code true} if present, {@code false} otherwise.
     *
     * @since 1.0.0
     */
    public boolean contains(Path path) {
        return containsKey(path);
    }

    /**
     * Checks if the mapping contains an entry for the given {@link File}.
     *
     * @param file The file.
     *
     * @return {@code true} if present, {@code false} otherwise.
     *
     * @since 1.0.0
     */
    public boolean contains(File file) {
        return containsKey(file);
    }

    /**
     * Returns the number of entries in the mapping.
     *
     * @return The size of the mapping.
     *
     * @since 1.0.0
     */
    public int size() {
        return fileMap.size();
    }

    /**
     * Checks if the mapping is empty.
     *
     * @return {@code true} if empty, {@code false} otherwise.
     *
     * @since 1.0.0
     */
    public boolean isEmpty() {
        return fileMap.isEmpty();
    }

}
