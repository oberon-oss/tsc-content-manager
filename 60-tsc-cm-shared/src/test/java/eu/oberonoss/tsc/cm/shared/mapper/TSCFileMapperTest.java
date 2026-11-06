package eu.oberonoss.tsc.cm.shared.mapper;

import eu.oberonoss.tsc.cm.base.cfg.BaseConfiguration;
import eu.oberonoss.tsc.cm.db.entity.TscScannedFileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TSCFileMapperTest {

    private TSCFileMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = TSCFileMapper.getInstance();
    }

    @Test
    void testSingletonInstance() {
        TSCFileMapper instance1 = TSCFileMapper.getInstance();
        TSCFileMapper instance2 = TSCFileMapper.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2);
        assertSame(mapper, instance1);
    }

    @Test
    void testResolvePath() {
        Path baseDir = BaseConfiguration.resolveDefaultInstallationDirectory();
        Path expected = baseDir.resolve("Assets/Kuju/RailSimulatorCore").resolve("Audio.bin").normalize();

        Path resolved = mapper.resolvePath("Assets/Kuju/RailSimulatorCore", "Audio.bin");
        assertEquals(expected, resolved);

        // Leading slashes handled
        Path resolvedLeadingSlash = mapper.resolvePath("/Assets/Kuju/RailSimulatorCore", "Audio.bin");
        assertEquals(expected, resolvedLeadingSlash);

        // Entity resolution
        TscScannedFileEntity entity = TscScannedFileEntity.builder()
                .filePath("Assets/Kuju/RailSimulatorCore")
                .fileName("Audio.bin")
                .fileSize(1024L)
                .fileType("bin")
                .build();

        assertEquals(expected, mapper.resolvePath(entity));
        assertNull(mapper.resolvePath(null));
    }

    @Test
    void testNoPublicMutatingMethods() {
        List<String> forbiddenMethodNames = List.of("put", "remove", "clear", "load", "set");
        Method[] publicMethods = TSCFileMapper.class.getMethods();

        for (Method method : publicMethods) {
            if (method.getDeclaringClass().equals(TSCFileMapper.class)) {
                assertFalse(
                        forbiddenMethodNames.contains(method.getName()),
                        "TSCFileMapper must not expose mutating or user-initiated load method: " + method.getName()
                );
            }
        }
    }

    @Test
    void testConstructorsArePrivate() {
        Constructor<?>[] constructors = TSCFileMapper.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));
    }

    @Test
    void testLookupMethodsWithNullAndNonExistentKeys() {
        assertNull(mapper.get((Path) null));
        assertNull(mapper.get((File) null));
        assertNull(mapper.get(null, null));

        assertFalse(mapper.containsKey((Path) null));
        assertFalse(mapper.containsKey((File) null));
        assertFalse(mapper.contains((Path) null));
        assertFalse(mapper.contains((File) null));

        Path nonExistent = Paths.get("non-existent-path-never-scanned-" + System.nanoTime());
        assertNull(mapper.get(nonExistent));
        assertNull(mapper.get(nonExistent.toFile()));
        assertNull(mapper.get("non-existent-dir", "non-existent-file.bin"));

        assertFalse(mapper.containsKey(nonExistent));
        assertFalse(mapper.containsKey(nonExistent.toFile()));
        assertFalse(mapper.contains(nonExistent));
        assertFalse(mapper.contains(nonExistent.toFile()));
    }

    @Test
    void testScannedFileInfoRecord() {
        Instant now = Instant.now();
        Instant scanned = now.plusSeconds(5);

        TscScannedFileEntity entity = TscScannedFileEntity.builder()
                .id(99L)
                .filePath("Assets/Test")
                .fileName("test.bin")
                .fileSize(4096L)
                .fileType("bin")
                .lastModified(now)
                .scannedAt(scanned)
                .build();

        ScannedFileInfo info = ScannedFileInfo.fromEntity(entity);
        assertNotNull(info);
        assertEquals(99L, info.id());
        assertEquals(99L, info.getId());
        assertEquals(4096L, info.fileSize());
        assertEquals(4096L, info.getFileSize());
        assertEquals("bin", info.fileType());
        assertEquals("bin", info.getFileType());
        assertEquals(now, info.lastModified());
        assertEquals(now, info.getLastModified());
        assertEquals(scanned, info.scannedAt());
        assertEquals(scanned, info.getScannedAt());

        assertNull(ScannedFileInfo.fromEntity(null));
    }

    @Test
    void testScannedFileInfoToBuilder() {
        ScannedFileInfo original = ScannedFileInfo.builder()
                .id(1L)
                .fileSize(100L)
                .fileType("bin")
                .lastModified(Instant.EPOCH)
                .scannedAt(Instant.EPOCH)
                .build();

        ScannedFileInfo modified = original.toBuilder()
                .fileSize(200L)
                .fileType("xml")
                .build();

        assertEquals(1L, modified.id());
        assertEquals(200L, modified.fileSize());
        assertEquals("xml", modified.fileType());
        assertEquals(Instant.EPOCH, modified.lastModified());
    }

    @Test
    void testIsEmpty() throws Exception {
        //noinspection SizeReplaceableByIsEmpty
        assertTrue(mapper.size() == 0 && mapper.isEmpty());

        Constructor<TSCFileMapper> constructor = TSCFileMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        TSCFileMapper testInstance = constructor.newInstance();
        //noinspection SizeReplaceableByIsEmpty
        assertTrue(testInstance.size() == 0 && testInstance.isEmpty());

        java.lang.reflect.Field field = TSCFileMapper.class.getDeclaredField("fileMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<Path, ScannedFileInfo> map =
                (java.util.concurrent.ConcurrentHashMap<Path, ScannedFileInfo>) field.get(testInstance);
        map.clear();
        assertTrue(testInstance.isEmpty());
        assertEquals(0, testInstance.size());

        map.put(Paths.get("test.bin"), ScannedFileInfo.builder().id(1L).build());
        assertFalse(testInstance.isEmpty());
        assertEquals(1, testInstance.size());
    }

    @Test
    void testPrivateConstructorExecution() throws Exception {
        Constructor<TSCFileMapper> constructor = TSCFileMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        TSCFileMapper testInstance = constructor.newInstance();
        assertNotNull(testInstance);
        assertNotNull(testInstance.resolvePath("dir", "file"));
    }
}
