package eu.oberonoss.tsc.cm.base;

/**
 * Implementation of the ByteLoader interface.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class ByteLoaderImpl implements ByteLoader {

    private ByteLoaderImpl() {
    }

    private static final ByteLoaderImpl INSTANCE = new ByteLoaderImpl();

    /**
     * Retrieves the singleton instance of ByteLoaderImpl.
     *
     * @return The singleton instance of ByteLoaderImpl.
     *
     * @since 1.0.0
     */
    public static ByteLoader getInstance() {
        return INSTANCE;
    }
}
