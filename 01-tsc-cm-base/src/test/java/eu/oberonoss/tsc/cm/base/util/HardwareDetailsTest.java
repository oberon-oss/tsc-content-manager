package eu.oberonoss.tsc.cm.base.util;

import eu.oberonoss.tsc.cm.base.util.HardwareDetails.HardwareDetailItem;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class HardwareDetailsTest {

    @Nested
    class Construction {

        @Test
        void constructorIsPrivate() throws NoSuchMethodException {
            Constructor<HardwareDetails> constructor = HardwareDetails.class.getDeclaredConstructor();

            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }

        @Test
        void privateConstructorCanBeInvokedReflectively() throws NoSuchMethodException {
            Constructor<HardwareDetails> constructor = HardwareDetails.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertDoesNotThrow((Executable) constructor::newInstance);
        }
    }

    @Nested
    class HardwareDetailItemEnum {

        @Test
        void enumContainsExpectedValues() {
            HardwareDetailItem[] values = HardwareDetailItem.values();
            assertEquals(2, values.length);
            assertArrayEquals(new HardwareDetailItem[]{
                    HardwareDetailItem.CPU_CORE_COUNT,
                    HardwareDetailItem.CPU_THREAD_COUNT
            }, values);
        }

        @Test
        void valueOfResolvesCorrectEnumConstant() {
            assertSame(HardwareDetailItem.CPU_CORE_COUNT, HardwareDetailItem.valueOf("CPU_CORE_COUNT"));
            assertSame(HardwareDetailItem.CPU_THREAD_COUNT, HardwareDetailItem.valueOf("CPU_THREAD_COUNT"));
        }

        @ParameterizedTest
        @EnumSource(HardwareDetailItem.class)
        void enumValuesAreNotNull(HardwareDetailItem item) {
            assertNotNull(item.name());
        }
    }

    @Nested
    class GetHardwareDetails {

        @Test
        void getCpuCoreCountReturnsPositiveInteger() {
            Integer coreCount = HardwareDetailItem.CPU_CORE_COUNT.getInfo();

            assertNotNull(coreCount);
            assertTrue(coreCount > 0, () -> "CPU core count should be greater than 0, got: " + coreCount);
        }

        @Test
        void getCpuThreadCountReturnsPositiveInteger() {
            Integer threadCount = HardwareDetailItem.CPU_THREAD_COUNT.getInfo();

            assertNotNull(threadCount);
            assertTrue(threadCount > 0, () -> "CPU thread count should be greater than 0, got: " + threadCount);
        }

        @Test
        void cpuThreadCountIsGreaterOrEqualToCoreCount() {
            Integer coreCount = HardwareDetailItem.CPU_CORE_COUNT.getInfo();
            Integer threadCount = HardwareDetailItem.CPU_THREAD_COUNT.getInfo();

            assertTrue(threadCount >= coreCount,
                    () -> String.format("Thread count (%d) should be >= core count (%d)", threadCount, coreCount));
        }

        @ParameterizedTest
        @EnumSource(HardwareDetailItem.class)
        void getHardwareDetailsReturnsNonNullPositiveIntegerForAllItems(HardwareDetailItem item) {
            Integer result = item.getInfo();

            assertNotNull(result);
            assertTrue(result > 0);
        }
    }
}
