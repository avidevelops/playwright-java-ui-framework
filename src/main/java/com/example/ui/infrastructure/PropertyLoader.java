package com.example.ui.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads {@code application.properties} from the classpath once at class initialisation.
 * All other infrastructure classes read configuration through this loader.
 */
public final class PropertyLoader {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = PropertyLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                PROPS.load(is);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load application.properties from classpath", e);
        }
    }

    private PropertyLoader() {}

    public static String get(String key) {
        return PROPS.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = PROPS.getProperty(key);
        return value != null ? Boolean.parseBoolean(value.trim()) : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String value = PROPS.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
