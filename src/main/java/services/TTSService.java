package services;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service pour la synthèse vocale (TTS) via Voice RSS API.
 */
public class TTSService {

    private static final String API_KEY = "957abcfdee56451293b6f38884fb7cd9";
    private static final String API_URL = "http://api.voicerss.org/";
    
    private MediaPlayer mediaPlayer;

    private double speed = 1.0;

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Lit le texte à voix haute.
     * @param text Le texte à lire.
     * @param lang La langue (ex: "fr-fr", "en-us").
     */
    public void speak(String text, String lang) {
        if (text == null || text.trim().isEmpty()) return;

        // Arrêter la lecture en cours si nécessaire
        stop();

        try {
            // Tronquer le texte brut AVANT l'encodage
            String textToEncode = text;
            if (textToEncode.length() > 1000) {
                textToEncode = textToEncode.substring(0, 1000);
            }

            String encodedText = URLEncoder.encode(textToEncode, StandardCharsets.UTF_8.toString())
                                          .replace("+", "%20");

            // Calcul du rate pour VoiceRSS (-10 à 10)
            int rate = (int) ((speed - 1.0) * 10);
            if (rate > 10) rate = 10;
            if (rate < -10) rate = -10;

            String url = String.format("%s?key=%s&hl=%s&src=%s&c=MP3&f=44khz_16bit_stereo&r=%d", 
                    API_URL, API_KEY, lang, encodedText, rate);

            System.out.println("Tentative de lecture audio (vitesse: " + speed + ")...");

            Media media = new Media(url);
            mediaPlayer = new MediaPlayer(media);
            
            mediaPlayer.setOnError(() -> {
                System.err.println("Erreur MediaPlayer : " + mediaPlayer.getError().getMessage());
            });

            mediaPlayer.setOnReady(() -> {
                System.out.println("Audio prêt à être lu.");
                mediaPlayer.play();
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                System.out.println("Fin de la lecture audio.");
            });

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du TTS : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}
