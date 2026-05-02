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
        cnx = MyDatabase.getInstance().getCnx();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // AJOUTER
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void add(Chapitre ch) throws SQLException {
        String req = "INSERT INTO chapitre (titre, contenu, ordre, duree, " +
                "pdf_url, id_cours, resume, type_explication, " +
                "explication, quiz_json) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1,  ch.getTitre());
        pst.setString(2,  ch.getContenu());
        pst.setInt   (3,  ch.getOrdre());
        pst.setInt   (4,  ch.getDuree());
        pst.setString(5,  ch.getPdfUrl());
        pst.setInt   (6,  ch.getIdCours());
        pst.setString(7,  ch.getResume());
        pst.setString(8,  ch.getTypeExplication());
        pst.setString(9,  ch.getExplication());
        pst.setString(10, ch.getQuizJson());
        pst.executeUpdate();

        // Récupérer l'id généré automatiquement
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            ch.setIdChapitre(rs.getInt(1));
        }

        System.out.println("✅ Chapitre ajouté : " + ch);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // MODIFIER
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void update(Chapitre ch) throws SQLException {
        String req = "UPDATE chapitre SET titre = ?, contenu = ?, ordre = ?, " +
                "duree = ?, pdf_url = ?, id_cours = ?, resume = ?, " +
                "type_explication = ?, explication = ?, quiz_json = ? " +
                "WHERE id_chapitre = ?";

        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1,  ch.getTitre());
        pst.setString(2,  ch.getContenu());
        pst.setInt   (3,  ch.getOrdre());
        pst.setInt   (4,  ch.getDuree());
        pst.setString(5,  ch.getPdfUrl());
        pst.setInt   (6,  ch.getIdCours());
        pst.setString(7,  ch.getResume());
        pst.setString(8,  ch.getTypeExplication());
        pst.setString(9,  ch.getExplication());
        pst.setString(10, ch.getQuizJson());
        pst.setInt   (11, ch.getIdChapitre());
        pst.executeUpdate();

        System.out.println("✅ Chapitre modifié : " + ch);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SUPPRIMER
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void delete(Chapitre ch) throws SQLException {
        String req = "DELETE FROM chapitre WHERE id_chapitre = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, ch.getIdChapitre());
        pst.executeUpdate();

        System.out.println("✅ Chapitre supprimé (id=" + ch.getIdChapitre() + ")");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // AFFICHER TOUS
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public List<Chapitre> getAll() throws SQLException {
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre ORDER BY id_cours, ordre";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            liste.add(mapRow(rs));
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // MÉTHODES UTILITAIRES
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void ajouter(Chapitre t) throws SQLException {
        add(t);
    }

    @Override
    public void modifier(Chapitre t) throws SQLException {
        update(t);
    }

    @Override
    public void supprimer(int id) throws SQLException {
        Chapitre ch = getById(id);
        if (ch != null) {
            delete(ch);
        }
    }

    @Override
    public List<Chapitre> afficher() throws SQLException {
        return getAll();
    }

    /** Récupérer tous les chapitres d'un cours donné */
    @Override
    public List<Chapitre> getByCours(int coursId) throws SQLException {
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre WHERE id_cours = ? ORDER BY ordre";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, coursId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            liste.add(mapRow(rs));
        }
        return liste;
    }

    /** Récupérer un chapitre par son id */
    @Override
    public Chapitre getById(int id) throws SQLException {
        String req = "SELECT * FROM chapitre WHERE id_chapitre = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }

    /** Convertit une ligne ResultSet en objet Chapitre */
    private Chapitre mapRow(ResultSet rs) throws SQLException {
        Chapitre ch = new Chapitre();
        ch.setIdChapitre     (rs.getInt("id_chapitre"));
        ch.setTitre          (rs.getString("titre"));
        ch.setContenu        (rs.getString("contenu"));
        ch.setOrdre          (rs.getInt("ordre"));
        ch.setDuree          (rs.getInt("duree"));
        ch.setPdfUrl         (rs.getString("pdf_url"));
        ch.setIdCours        (rs.getInt("id_cours"));
        ch.setResume         (rs.getString("resume"));
        ch.setTypeExplication(rs.getString("type_explication"));
        ch.setExplication    (rs.getString("explication"));
        ch.setQuizJson       (rs.getString("quiz_json"));
        return ch;
    }
}
