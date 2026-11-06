package eu.oberonoss.tsc.cm.shared.i18n;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Centralized internationalization (i18n) utility for TSC Content Manager. Provides localized messages, formatting support, and resource bundle access across
 * all modules.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
@Slf4j
public final class I18n {

    /**
     * Base name for the application resource bundles.
     */
    public static final String BUNDLE_BASE_NAME = "i18n.messages";

    private static volatile Locale currentLocale = Locale.getDefault();

    private I18n() {
        // Utility class
    }

    /**
     * Retrieves the current application locale.
     *
     * @return The active {@link Locale}.
     */
    public static Locale getLocale() {
        return currentLocale;
    }

    /**
     * Sets the active application locale for message resolution. If null is provided, defaults to {@link Locale#getDefault()}.
     *
     * @param locale The {@link Locale} to set.
     */
    public static void setLocale(Locale locale) {
        currentLocale = (locale != null) ? locale : Locale.getDefault();
    }

    /**
     * Resets the active locale to the system default locale.
     */
    public static void resetLocale() {
        currentLocale = Locale.getDefault();
    }

    /**
     * Retrieves the underlying {@link ResourceBundle} for the currently active locale.
     *
     * @return The {@link ResourceBundle} instance.
     */
    public static ResourceBundle getBundle() {
        return getBundle(currentLocale);
    }

    /**
     * Retrieves the underlying {@link ResourceBundle} for a specific locale.
     *
     * @param locale The target {@link Locale}.
     *
     * @return The {@link ResourceBundle} instance.
     */
    public static ResourceBundle getBundle(Locale locale) {
        Locale targetLocale = (locale != null) ? locale : currentLocale;
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, targetLocale, I18n.class.getClassLoader());
    }

    /**
     * Checks if a message key exists in the bundle for the current locale.
     *
     * @param key The message key to check.
     *
     * @return True if key exists, false otherwise.
     */
    public static boolean containsKey(String key) {
        return containsKey(currentLocale, key);
    }

    /**
     * Checks if a message key exists in the bundle for a specific locale.
     *
     * @param locale The target {@link Locale}.
     * @param key    The message key to check.
     *
     * @return True if key exists, false otherwise.
     */
    public static boolean containsKey(Locale locale, String key) {
        if (key == null) {
            return false;
        }
        try {
            return getBundle(locale).containsKey(key);
        } catch (MissingResourceException e) {
            return false;
        }
    }

    /**
     * Resolves a localized string for the given key using the active locale.
     *
     * @param key The message key.
     *
     * @return The localized message string, or {@code !key!} if not found.
     */
    public static String get(String key) {
        return get(currentLocale, key);
    }

    /**
     * Resolves a localized string for a specific locale.
     *
     * @param locale The target {@link Locale}.
     * @param key    The message key.
     *
     * @return The localized message string, or {@code !key!} if not found.
     */
    public static String get(Locale locale, String key) {
        if (key == null) {
            return "!null!";
        }
        try {
            return getBundle(locale).getString(key);
        } catch (MissingResourceException e) {
            LOGGER.warn("Missing i18n message key: '{}' for locale: '{}'", key, locale);
            return '!' + key + '!';
        }
    }

    /**
     * Resolves a localized string and formats it with positional parameters using the active locale.
     *
     * @param key  The message key.
     * @param args The arguments to format into the message pattern.
     *
     * @return The formatted message string.
     */
    public static String format(String key, Object... args) {
        return format(currentLocale, key, args);
    }

    /**
     * Resolves a localized string for a specific locale and formats it with positional parameters.
     *
     * @param locale The target {@link Locale}.
     * @param key    The message key.
     * @param args   The arguments to format into the message pattern.
     *
     * @return The formatted message string.
     */
    public static String format(Locale locale, String key, Object... args) {
        String pattern = get(locale, key);
        if (args == null || args.length == 0) {
            return pattern;
        }
        try {
            MessageFormat messageFormat = new MessageFormat(pattern, (locale != null) ? locale : currentLocale);
            return messageFormat.format(args);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed to format message pattern '{}' with arguments: {}", pattern, args, e);
            return pattern;
        }
    }
}
