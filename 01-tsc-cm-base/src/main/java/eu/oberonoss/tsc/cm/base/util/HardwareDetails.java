package eu.oberonoss.tsc.cm.base.util;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.function.Supplier;

/**
 * Utility class for retrieving hardware details.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */

public final class HardwareDetails {
    private HardwareDetails() {
        // Class cannot be instantiated
    }

    private static final HardwareAbstractionLayer HARDWARE_ABSTRACTION_LAYER = new SystemInfo().getHardware();

    /**
     * Lists the (currently) supported hardware detail items.
     *
     * @since 1.0.0
     */
    public enum HardwareDetailItem {
        /**
         * Represents the number of physical CPU cores.
         *
         * @since 1.0.0
         */
        CPU_CORE_COUNT(() -> HARDWARE_ABSTRACTION_LAYER.getProcessor().getPhysicalProcessorCount()),

        /**
         * Represents the number of logical CPU threads.
         *
         * @since 1.0.0
         */
        CPU_THREAD_COUNT(() -> HARDWARE_ABSTRACTION_LAYER.getProcessor().getLogicalProcessorCount());

        private final Supplier<Object> supplier;

        HardwareDetailItem(Supplier<Object> function) {
            this.supplier = function;
        }

        /**
         * Retrieves information provided by the associated supplier.
         *
         * @param <T> The type of information returned by the supplier.
         *
         * @return The information retrieved from the supplier, cast to the expected type.
         *
         * @since 1.0.0
         */
        public <T> T getInfo() {
            //noinspection unchecked
            return (T) supplier.get();
        }
    }
}
