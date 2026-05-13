package com.skillora.payment;

import java.awt.Desktop;
import java.net.URI;

/**
 * Utilitaires pour le module de paiement.
 */
public class PaymentUtils {
    
    /**
     * Ouvre une URL dans le navigateur par défaut de l'utilisateur.
     */
    public static void openBrowser(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                System.err.println("Impossible d'ouvrir le navigateur : " + e.getMessage());
            }
        } else {
            // Alternative pour certains OS
            try {
                Runtime runtime = Runtime.getRuntime();
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    runtime.exec("open " + url);
                } else {
                    runtime.exec("xdg-open " + url);
                }
            } catch (Exception e) {
                System.err.println("Erreur ouverture alternative navigateur : " + e.getMessage());
            }
        }
    }
}
