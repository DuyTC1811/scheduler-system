package org.scheduler.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class YamlConfigLoader {

    private static final String CONFIG_FILE = "config.yaml";
    private static final Map<String, Object> config;

    static {
        try (InputStream input = YamlConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Config file not found: " + CONFIG_FILE);
            }
            Yaml yaml = new Yaml();
            config = yaml.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML config", e);
        }
    }

    public static Object getProperty(String key) {
        String[] keys = key.split("\\.");
        Object current = config;

        for (int i = 0; i < keys.length - 1; i++) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(keys[i]);
            } else {
                return null;
            }
        }

        if (current instanceof Map<?, ?> map) {
            return map.get(keys[keys.length - 1]);
        }

        return null;
    }

    public static String getString(String key) {
        Object value = getProperty(key);
        return (value instanceof String str) ? str : null;
    }

    public static int getInt(String key) {
        Object value = getProperty(key);
        if (value instanceof Integer i) return i;
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {}
        }
        return 0; // hoặc ném exception nếu muốn strict
    }

    public static boolean getBoolean(String key) {
        Object value = getProperty(key);
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) {
            return Boolean.parseBoolean(s); // "true"/"false"
        }
        return false;
    }
}
