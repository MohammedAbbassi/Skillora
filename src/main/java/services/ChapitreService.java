package services;

import Interfaces.IChapitreService;
import entities.Chapitre;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChapitreService implements IChapitreService {

    private final Connection cnx;

    public ChapitreService() {
        this.cnx = MyDatabase.getInstance().getCnx();
    }

    private void checkConnection() throws SQLException {
        if (cnx == null) {
            throw new SQLException("Base de données non connectée. Veuillez vérifier votre configuration MySQL.");
        }
    }

    @Override
    public void add(Chapitre ch) throws SQLException {
        checkConnection();
        String req = "INSERT INTO chapitre (titre, contenu, duree, " +
                "pdf_url, id_cours, type_explication, " +
                "explication, quiz_json, niveau, youtube_link, est_complete, remarques, fichiers_tp, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1,  ch.getTitre());
        pst.setString(2,  ch.getContenu());
        pst.setInt   (3,  ch.getDuree());
        pst.setString(4,  ch.getPdfUrl());
        pst.setInt   (5,  ch.getIdCours());
        pst.setString(6,  ch.getTypeExplication());
        pst.setString(7,  ch.getExplication());
        pst.setString(8,  ch.getQuizJson());
        pst.setString(9,  ch.getNiveau());
        pst.setString(10, ch.getYoutubeLink());
        pst.setBoolean(11, ch.isEstComplete());
        pst.setString(12, ch.getRemarques());
        pst.setString(13, ch.getFichiersTp());
        pst.setString(14, ch.getImageUrl());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            ch.setIdChapitre(rs.getInt(1));
        }
    }

    @Override
    public void update(Chapitre ch) throws SQLException {
        checkConnection();
        String req = "UPDATE chapitre SET titre = ?, contenu = ?, " +
                "duree = ?, pdf_url = ?, id_cours = ?, " +
                "type_explication = ?, explication = ?, quiz_json = ?, " +
                "niveau = ?, youtube_link = ?, est_complete = ?, remarques = ?, fichiers_tp = ?, image_url = ? " +
                "WHERE id_chapitre = ?";

        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1,  ch.getTitre());
        pst.setString(2,  ch.getContenu());
        pst.setInt   (3,  ch.getDuree());
        pst.setString(4,  ch.getPdfUrl());
        pst.setInt   (5,  ch.getIdCours());
        pst.setString(6,  ch.getTypeExplication());
        pst.setString(7,  ch.getExplication());
        pst.setString(8,  ch.getQuizJson());
        pst.setString(9,  ch.getNiveau());
        pst.setString(10, ch.getYoutubeLink());
        pst.setBoolean(11, ch.isEstComplete());
        pst.setString(12, ch.getRemarques());
        pst.setString(13, ch.getFichiersTp());
        pst.setString(14, ch.getImageUrl());
        pst.setInt(15, ch.getIdChapitre());
        pst.executeUpdate();
    }

    public void updateUserData(int id, String remarques, String fichiersTp) throws SQLException {
        checkConnection();
        String req = "UPDATE chapitre SET remarques = ?, fichiers_tp = ? WHERE id_chapitre = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, remarques);
        pst.setString(2, fichiersTp);
        pst.setInt(3, id);
        pst.executeUpdate();
    }

    public void delete(Chapitre ch) throws SQLException {
        checkConnection();
        String req = "DELETE FROM chapitre WHERE id_chapitre = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, ch.getIdChapitre());
        pst.executeUpdate();
    }

    @Override
    public List<Chapitre> getAll() throws SQLException {
        checkConnection();
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre ORDER BY id_cours, id_chapitre";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public void ajouter(Chapitre t) throws SQLException { add(t); }

    @Override
    public void modifier(Chapitre t) throws SQLException { update(t); }

    @Override
    public void supprimer(int id) throws SQLException {
        Chapitre ch = getById(id);
        if (ch != null) delete(ch);
    }

    @Override
    public List<Chapitre> afficher() throws SQLException { return getAll(); }

    @Override
    public List<Chapitre> getByCours(int coursId) throws SQLException {
        checkConnection();
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre WHERE id_cours = ? ORDER BY id_chapitre";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, coursId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public Chapitre getById(int id) throws SQLException {
        checkConnection();
        String req = "SELECT * FROM chapitre WHERE id_chapitre = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) return mapRow(rs);
        return null;
    }

    public void updateCompletionStatus(int id, boolean status) throws SQLException {
        checkConnection();
        String req = "UPDATE chapitre SET est_complete = ? WHERE id_chapitre = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setBoolean(1, status);
        pst.setInt(2, id);
        pst.executeUpdate();
    }

    private Chapitre mapRow(ResultSet rs) throws SQLException {
        Chapitre ch = new Chapitre();
        ch.setIdChapitre     (rs.getInt("id_chapitre"));
        ch.setTitre          (rs.getString("titre"));
        ch.setContenu        (rs.getString("contenu"));
        ch.setDuree          (rs.getInt("duree"));
        ch.setPdfUrl         (rs.getString("pdf_url"));
        ch.setIdCours        (rs.getInt("id_cours"));
        ch.setTypeExplication(rs.getString("type_explication"));
        ch.setExplication    (rs.getString("explication"));
        ch.setQuizJson       (rs.getString("quiz_json"));
        ch.setNiveau         (rs.getString("niveau"));
        ch.setYoutubeLink    (rs.getString("youtube_link"));
        ch.setEstComplete    (rs.getBoolean("est_complete"));
        ch.setRemarques      (rs.getString("remarques"));
        ch.setFichiersTp     (rs.getString("fichiers_tp"));
        ch.setImageUrl       (rs.getString("image_url"));
        return ch;
    }
}
