package services;

import Interfaces.ICoursService;
import entities.Cours;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursService implements ICoursService {

    private final Connection cnx;

    public CoursService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // AJOUTER
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void add(Cours cours) throws SQLException {
        String req = "INSERT INTO cours (titre, description, domaine, niveau, " +
                "duree, date_creation, id_instructeur, progression) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, cours.getTitre());
        pst.setString(2, cours.getDescription());
        pst.setString(3, cours.getDomaine());
        pst.setString(4, cours.getNiveau());
        pst.setInt   (5, cours.getDuree());
        pst.setDate  (6, cours.getDateCreation() != null
                ? Date.valueOf(cours.getDateCreation()) : null);
        if (cours.getIdInstructeur() != null) {
            pst.setLong(7, cours.getIdInstructeur());
        } else {
            pst.setNull(7, Types.BIGINT);
        }
        pst.setInt   (8, cours.getProgression());
        pst.executeUpdate();

        // Récupérer l'id généré automatiquement
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            cours.setIdCours(rs.getInt(1));
        }

        System.out.println("✅ Cours ajouté : " + cours);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // MODIFIER
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void update(Cours cours) throws SQLException {
        String req = "UPDATE cours SET titre = ?, description = ?, domaine = ?, " +
                "niveau = ?, duree = ?, date_creation = ?, " +
                "id_instructeur = ?, progression = ? " +
                "WHERE id_cours = ?";

        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, cours.getTitre());
        pst.setString(2, cours.getDescription());
        pst.setString(3, cours.getDomaine());
        pst.setString(4, cours.getNiveau());
        pst.setInt   (5, cours.getDuree());
        pst.setDate  (6, cours.getDateCreation() != null
                ? Date.valueOf(cours.getDateCreation()) : null);
        if (cours.getIdInstructeur() != null) {
            pst.setLong(7, cours.getIdInstructeur());
        } else {
            pst.setNull(7, Types.BIGINT);
        }
        pst.setInt   (8, cours.getProgression());
        pst.setInt   (9, cours.getIdCours());
        pst.executeUpdate();

        System.out.println("✅ Cours modifié : " + cours);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SUPPRIMER
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void delete(Cours cours) throws SQLException {
        // Grâce au ON DELETE CASCADE défini sur chapitre,
        // les chapitres liés seront supprimés automatiquement.
        String req = "DELETE FROM cours WHERE id_cours = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, cours.getIdCours());
        pst.executeUpdate();

        System.out.println("✅ Cours supprimé (id=" + cours.getIdCours() + ")");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // AFFICHER TOUS
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public List<Cours> getAll() throws SQLException {
        List<Cours> liste = new ArrayList<>();
        String req = "SELECT * FROM cours ORDER BY id_cours";
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
    public void ajouter(Cours t) throws SQLException {
        add(t);
    }

    @Override
    public void modifier(Cours t) throws SQLException {
        update(t);
    }

    @Override
    public void supprimer(int id) throws SQLException {
        Cours c = getById(id);
        if (c != null) {
            delete(c);
        }
    }

    @Override
    public List<Cours> afficher() throws SQLException {
        return getAll();
    }

    /** Récupérer un cours par son id */
    @Override
    public Cours getById(int id) throws SQLException {
        String req = "SELECT * FROM cours WHERE id_cours = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }

    /** Convertit une ligne ResultSet en objet Cours */
    private Cours mapRow(ResultSet rs) throws SQLException {
        Cours c = new Cours();
        c.setIdCours    (rs.getInt("id_cours"));
        c.setTitre      (rs.getString("titre"));
        c.setDescription(rs.getString("description"));
        c.setDomaine    (rs.getString("domaine"));
        c.setNiveau     (rs.getString("niveau"));
        c.setDuree      (rs.getInt("duree"));

        Date d = rs.getDate("date_creation");
        c.setDateCreation(d != null ? d.toLocalDate() : null);

        long idInstructeur = rs.getLong("id_instructeur");
        c.setIdInstructeur(rs.wasNull() ? null : idInstructeur);
        c.setProgression(rs.getInt("progression"));
        return c;
    }
}
