package com.skillora.shop.config;

import java.util.Properties;

/**
 * Configuration de l'application (permet d'extraire les paramètres vers un fichier properties plus tard).
 */
public class AppConfig {
    private static final Properties props = new Properties();

    static {
        // Valeurs par défaut
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/skillora_shop?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "");
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static String appTitle() {
        return "Skillora - Boutique & Commandes";
    }
}
