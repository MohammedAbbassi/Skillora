package com.skillora.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (Exception e) {
            System.err.println("application.properties: " + e.getMessage());
        }
        String external = System.getProperty("skillora.config");
        if (external != null && !external.isBlank()) {
            Path p = Paths.get(external.trim());
            try {
                if (Files.isRegularFile(p)) {
                    try (InputStream in = Files.newInputStream(p)) {
                        PROPS.load(in);
                    }
                }
            } catch (Exception e) {
                System.err.println("skillora.config: " + e.getMessage());
            }
        }
    }

    private AppConfig() {
    }

    public static String get(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue);
    }

    public static String appTitle() {
        return get("app.name", "Skillora Shop") + " — " + get("app.tagline", "Boutique");
    }
}
