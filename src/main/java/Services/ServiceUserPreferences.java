package services;

import entities.UserPreferences;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUserPreferences implements IService<UserPreferences> {

    private Connection cnx;

    public ServiceUserPreferences() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(UserPreferences prefs) throws SQLException {
        String req = "INSERT INTO preferences_utilisateur(id_utilisateur, type_police, taille_police, interligne, espacement_lettres, couleur_fond, couleur_texte, synthese_vocale, surlignage_lecture, reduire_animations) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setLong(1, prefs.getIdUtilisateur());
        pst.setString(2, prefs.getTypePolice());
        pst.setInt(3, prefs.getTaillePolice());
        pst.setDouble(4, prefs.getInterligne());
        pst.setDouble(5, prefs.getEspacementLettres());
        pst.setString(6, prefs.getCouleurFond());
        pst.setString(7, prefs.getCouleurTexte());
        pst.setBoolean(8, prefs.isSyntheseVocale());
        pst.setBoolean(9, prefs.isSurlignageLecture());
        pst.setBoolean(10, prefs.isReduireAnimations());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            prefs.setIdPreference(rs.getLong(1));
        }
        System.out.println("Preferences ajoutées");
    }

    @Override
    public void update(UserPreferences prefs) throws SQLException {
        String req = "UPDATE preferences_utilisateur SET type_police = ?, taille_police = ?, interligne = ?, espacement_lettres = ?, couleur_fond = ?, couleur_texte = ?, synthese_vocale = ?, surlignage_lecture = ?, reduire_animations = ? WHERE id_utilisateur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, prefs.getTypePolice());
        pst.setInt(2, prefs.getTaillePolice());
        pst.setDouble(3, prefs.getInterligne());
        pst.setDouble(4, prefs.getEspacementLettres());
        pst.setString(5, prefs.getCouleurFond());
        pst.setString(6, prefs.getCouleurTexte());
        pst.setBoolean(7, prefs.isSyntheseVocale());
        pst.setBoolean(8, prefs.isSurlignageLecture());
        pst.setBoolean(9, prefs.isReduireAnimations());
        pst.setLong(10, prefs.getIdUtilisateur());
        pst.executeUpdate();
        System.out.println("Preferences modifiées");
    }

    @Override
    public void delete(UserPreferences prefs) throws SQLException {
        String req = "DELETE FROM preferences_utilisateur WHERE id_utilisateur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, prefs.getIdUtilisateur());
        pst.executeUpdate();
    }

    @Override
    public List<UserPreferences> getAll() throws SQLException {
        List<UserPreferences> prefsList = new ArrayList<>();
        String req = "SELECT * FROM preferences_utilisateur";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            UserPreferences prefs = new UserPreferences();
            prefs.setIdPreference(rs.getLong("id_preference"));
            prefs.setIdUtilisateur(rs.getLong("id_utilisateur"));
            prefs.setTypePolice(rs.getString("type_police"));
            prefs.setTaillePolice(rs.getInt("taille_police"));
            prefs.setInterligne(rs.getDouble("interligne"));
            prefs.setEspacementLettres(rs.getDouble("espacement_lettres"));
            prefs.setCouleurFond(rs.getString("couleur_fond"));
            prefs.setCouleurTexte(rs.getString("couleur_texte"));
            prefs.setSyntheseVocale(rs.getBoolean("synthese_vocale"));
            prefs.setSurlignageLecture(rs.getBoolean("surlignage_lecture"));
            prefs.setReduireAnimations(rs.getBoolean("reduire_animations"));
            prefsList.add(prefs);
        }
        return prefsList;
    }

    public UserPreferences getByUserId(long userId) throws SQLException {
        String req = "SELECT * FROM preferences_utilisateur WHERE id_utilisateur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, userId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            UserPreferences prefs = new UserPreferences();
            prefs.setIdPreference(rs.getLong("id_preference"));
            prefs.setIdUtilisateur(rs.getLong("id_utilisateur"));
            prefs.setTypePolice(rs.getString("type_police"));
            prefs.setTaillePolice(rs.getInt("taille_police"));
            prefs.setInterligne(rs.getDouble("interligne"));
            prefs.setEspacementLettres(rs.getDouble("espacement_lettres"));
            prefs.setCouleurFond(rs.getString("couleur_fond"));
            prefs.setCouleurTexte(rs.getString("couleur_texte"));
            prefs.setSyntheseVocale(rs.getBoolean("synthese_vocale"));
            prefs.setSurlignageLecture(rs.getBoolean("surlignage_lecture"));
            prefs.setReduireAnimations(rs.getBoolean("reduire_animations"));
            return prefs;
        }
        return null;
    }
}