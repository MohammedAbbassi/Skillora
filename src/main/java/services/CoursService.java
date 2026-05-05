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

    @Override
    public void add(Cours cours) throws SQLException {
        String req = "INSERT INTO cours (titre, description, categorie, niveau, " +
                "duree, objectif_semaine, performance, date_creation, progression) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, cours.getTitre());
        pst.setString(2, cours.getDescription());
        pst.setString(3, cours.getCategorie());
        pst.setString(4, cours.getNiveau());
        pst.setString(5, cours.getDuree());
        pst.setString(6, cours.getObjectifSemaine());
        pst.setString(7, cours.getPerformance());
        pst.setDate  (8, cours.getDateCreation() != null ? Date.valueOf(cours.getDateCreation()) : null);
        pst.setInt   (9, cours.getProgression());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            cours.setIdCours(rs.getInt(1));
        }
    }

    @Override
    public void update(Cours cours) throws SQLException {
        String req = "UPDATE cours SET titre = ?, description = ?, categorie = ?, " +
                "niveau = ?, duree = ?, objectif_semaine = ?, performance = ?, " +
                "date_creation = ?, progression = ? " +
                "WHERE id_cours = ?";

        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, cours.getTitre());
        pst.setString(2, cours.getDescription());
        pst.setString(3, cours.getCategorie());
        pst.setString(4, cours.getNiveau());
        pst.setString(5, cours.getDuree());
        pst.setString(6, cours.getObjectifSemaine());
        pst.setString(7, cours.getPerformance());
        pst.setDate  (8, cours.getDateCreation() != null ? Date.valueOf(cours.getDateCreation()) : null);
        pst.setInt   (9, cours.getProgression());
        pst.setInt   (10, cours.getIdCours());
        pst.executeUpdate();
    }

    @Override
    public void delete(Cours cours) throws SQLException {
        String req = "DELETE FROM cours WHERE id_cours = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, cours.getIdCours());
        pst.executeUpdate();
    }

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

    @Override
    public void ajouter(Cours t) throws SQLException { add(t); }

    @Override
    public void modifier(Cours t) throws SQLException { update(t); }

    @Override
    public void supprimer(int id) throws SQLException {
        Cours c = getById(id);
        if (c != null) delete(c);
    }

    @Override
    public List<Cours> afficher() throws SQLException { return getAll(); }

    @Override
    public Cours getById(int id) throws SQLException {
        String req = "SELECT * FROM cours WHERE id_cours = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) return mapRow(rs);
        return null;
    }

    private Cours mapRow(ResultSet rs) throws SQLException {
        Cours c = new Cours();
        c.setIdCours    (rs.getInt("id_cours"));
        c.setTitre      (rs.getString("titre"));
        c.setDescription(rs.getString("description"));
        c.setCategorie  (rs.getString("categorie"));
        c.setNiveau     (rs.getString("niveau"));
        c.setDuree      (rs.getString("duree"));
        c.setObjectifSemaine(rs.getString("objectif_semaine"));
        c.setPerformance(rs.getString("performance"));

        Date dCreation = rs.getDate("date_creation");
        c.setDateCreation(dCreation != null ? dCreation.toLocalDate() : null);

        c.setProgression(rs.getInt("progression"));
        return c;
    }
}


