package services;

import entities.UserPreference;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DyslexiaService {

    private static final String[] SYLLABLE_COLORS = {"#3b82f6", "#ef4444", "#10b981", "#f59e0b"};

    public UserPreference getPreferences(long userId) {
        String sql = "SELECT * FROM preferences_utilisateur WHERE id_utilisateur = ?";
        Connection conn = MyDatabase.getInstance().getCnx();
        if (conn == null) return createDefaultPreferences(userId);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserPreference pref = new UserPreference();
                pref.setIdPreference(rs.getLong("id_preference"));
                pref.setIdUtilisateur(rs.getLong("id_utilisateur"));
                pref.setTypePolice(rs.getString("type_police"));
                pref.setTaillePolice(rs.getInt("taille_police"));
                pref.setInterligne(rs.getDouble("interligne"));
                pref.setEspacementLettres(rs.getDouble("espacement_lettres"));
                pref.setEspacementMots(rs.getDouble("espacement_mots"));
                pref.setCouleurFond(rs.getString("couleur_fond"));
                pref.setCouleurTexte(rs.getString("couleur_texte"));
                pref.setModeSyllabique(rs.getBoolean("mode_syllabique"));
                pref.setColorationSyllabes(rs.getBoolean("coloration_syllabes"));
                pref.setFocusLigne(rs.getBoolean("focus_ligne"));
                pref.setVitesseAudio(rs.getDouble("vitesse_audio"));
                pref.setSyntheseVocale(rs.getBoolean("synthese_vocale"));
                pref.setSurlignageLecture(rs.getBoolean("surlignage_lecture"));
                pref.setReduireAnimations(rs.getBoolean("reduire_animations"));
                return pref;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new UserPreference(); // Default
    }

    public void savePreferences(UserPreference pref) {
        String sql = "INSERT INTO preferences_utilisateur (id_utilisateur, type_police, taille_police, interligne, " +
                "espacement_lettres, espacement_mots, couleur_fond, couleur_texte, mode_syllabique, " +
                "coloration_syllabes, focus_ligne, vitesse_audio, synthese_vocale, surlignage_lecture, reduire_animations) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE type_police=?, taille_police=?, interligne=?, espacement_lettres=?, " +
                "espacement_mots=?, couleur_fond=?, couleur_texte=?, mode_syllabique=?, coloration_syllabes=?, " +
                "focus_ligne=?, vitesse_audio=?, synthese_vocale=?, surlignage_lecture=?, reduire_animations=?";
        
        Connection conn = MyDatabase.getInstance().getCnx();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pref.getIdUtilisateur());
            ps.setString(2, pref.getTypePolice());
            ps.setInt(3, pref.getTaillePolice());
            ps.setDouble(4, pref.getInterligne());
            ps.setDouble(5, pref.getEspacementLettres());
            ps.setDouble(6, pref.getEspacementMots());
            ps.setString(7, pref.getCouleurFond());
            ps.setString(8, pref.getCouleurTexte());
            ps.setBoolean(9, pref.isModeSyllabique());
            ps.setBoolean(10, pref.isColorationSyllabes());
            ps.setBoolean(11, pref.isFocusLigne());
            ps.setDouble(12, pref.getVitesseAudio());
            ps.setBoolean(13, pref.isSyntheseVocale());
            ps.setBoolean(14, pref.isSurlignageLecture());
            ps.setBoolean(15, pref.isReduireAnimations());

            // Updates
            ps.setString(16, pref.getTypePolice());
            ps.setInt(17, pref.getTaillePolice());
            ps.setDouble(18, pref.getInterligne());
            ps.setDouble(19, pref.getEspacementLettres());
            ps.setDouble(20, pref.getEspacementMots());
            ps.setString(21, pref.getCouleurFond());
            ps.setString(22, pref.getCouleurTexte());
            ps.setBoolean(23, pref.isModeSyllabique());
            ps.setBoolean(24, pref.isColorationSyllabes());
            ps.setBoolean(25, pref.isFocusLigne());
            ps.setDouble(26, pref.getVitesseAudio());
            ps.setBoolean(27, pref.isSyntheseVocale());
            ps.setBoolean(28, pref.isSurlignageLecture());
            ps.setBoolean(29, pref.isReduireAnimations());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sépare un mot en syllabes (Algorithme simplifié pour le français).
     */
    public List<String> splitIntoSyllables(String word) {
        List<String> syllables = new ArrayList<>();
        if (word.length() <= 3) {
            syllables.add(word);
            return syllables;
        }

        // Regex simplifiée pour les syllabes françaises
        // On cherche une consonne suivie d'une voyelle, ou des groupes de consonnes
        String regex = "(?i)([^aeiouyàâéèêëîïôûù]*[aeiouyàâéèêëîïôûù]+(?:[^aeiouyàâéèêëîïôûù](?![aeiouyàâéèêëîïôûù]))*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(word);

        while (matcher.find()) {
            syllables.add(matcher.group());
        }

        if (syllables.isEmpty()) syllables.add(word);
        return syllables;
    }

    /**
     * Crée des noeuds Text pour un TextFlow selon les préférences.
     */
    public List<Text> processText(String content, UserPreference pref) {
        List<Text> textNodes = new ArrayList<>();
        String[] words = content.split("(\\s+)");
        
        // On récupère aussi les séparateurs (espaces, retours à la ligne)
        Pattern p = Pattern.compile("(\\s+|\\n)");
        Matcher m = p.matcher(content);
        List<String> separators = new ArrayList<>();
        while (m.find()) {
            separators.add(matcherToGroup(m));
        }

        int sepIdx = 0;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            
            // Traiter le contenu du mot
            if (pref.isModeSyllabique()) {
                List<String> syllables = splitIntoSyllables(word);
                for (int j = 0; j < syllables.size(); j++) {
                    List<Text> syllableNodes = createWordNodes(syllables.get(j), pref, j);
                    for (Text t : syllableNodes) {
                        t.setUserData(word); // Garder le mot complet pour le dictionnaire
                        t.setCursor(javafx.scene.Cursor.HAND);
                    }
                    textNodes.addAll(syllableNodes);
                    if (j < syllables.size() - 1 && !pref.isColorationSyllabes()) {
                        textNodes.add(new Text("-"));
                    }
                }
            } else {
                List<Text> wordNodes = createWordNodes(word, pref, -1);
                for (Text t : wordNodes) {
                    t.setUserData(word);
                    t.setCursor(javafx.scene.Cursor.HAND);
                }
                textNodes.addAll(wordNodes);
            }

            // Traiter l'espacement entre les mots
            if (sepIdx < separators.size()) {
                String sepStr = separators.get(sepIdx++);
                Text sep = new Text(sepStr);
                applyStyle(sep, pref, -1);
                
                // Augmenter l'espace si l'espacement des mots est réglé
                if (sepStr.equals(" ") && pref.getEspacementMots() > 1.0) {
                    int extraSpaces = (int) Math.round(pref.getEspacementMots());
                    sep.setText(" ".repeat(Math.max(1, extraSpaces)));
                }
                textNodes.add(sep);
            }
        }
        return textNodes;
    }

    private List<Text> createWordNodes(String part, UserPreference pref, int syllableIdx) {
        List<Text> nodes = new ArrayList<>();
        // Si l'espacement des lettres est élevé, on découpe par caractère
        if (pref.getEspacementLettres() > 0.1) {
            for (char c : part.toCharArray()) {
                Text t = new Text(String.valueOf(c));
                applyStyle(t, pref, syllableIdx);
                nodes.add(t);
                // On ajoute un petit espace invisible entre les caractères
                Text spacer = new Text("\u200A"); // Hair space
                nodes.add(spacer);
            }
        } else {
            Text t = new Text(part);
            applyStyle(t, pref, syllableIdx);
            nodes.add(t);
        }
        return nodes;
    }

    private String matcherToGroup(Matcher m) {
        return m.group();
    }

    private void applyStyle(Text t, UserPreference pref, int syllableIdx) {
        t.setFont(Font.font(pref.getTypePolice(), pref.getTaillePolice()));
        
        if (pref.isColorationSyllabes() && syllableIdx != -1) {
            t.setFill(Color.web(SYLLABLE_COLORS[syllableIdx % SYLLABLE_COLORS.length]));
        } else {
            t.setFill(Color.web(pref.getCouleurTexte()));
        }
        
        // Espacement des lettres via CSS style car Text n'a pas de propriété directe
        t.setStyle("-fx-letter-spacing: " + pref.getEspacementLettres() + "em;");
    }

    private UserPreference createDefaultPreferences(long userId) {
        UserPreference pref = new UserPreference();
        pref.setIdUtilisateur(userId);
        pref.setTypePolice("Arial");
        pref.setTaillePolice(18);
        pref.setInterligne(1.5);
        pref.setEspacementLettres(0.1);
        pref.setEspacementMots(1.0);
        pref.setCouleurFond("#FFFFFF");
        pref.setCouleurTexte("#1E293B");
        pref.setModeSyllabique(false);
        pref.setColorationSyllabes(false);
        pref.setFocusLigne(false);
        pref.setVitesseAudio(1.0);
        pref.setSyntheseVocale(true);
        pref.setSurlignageLecture(false);
        pref.setReduireAnimations(false);
        return pref;
    }
}
