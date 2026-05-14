package com.skillora.shop.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/skillora_shop?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "");

        try (InputStream in = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("[ShopConfig] Cannot load application.properties: " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static String appTitle() {
        return "Skillora - Boutique & Commandes";
    }
}
