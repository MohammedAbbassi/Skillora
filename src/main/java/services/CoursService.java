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
        this.cnx = MyDatabase.getInstance().getCnx();
    }

    private void checkConnection() throws SQLException {
        if (cnx == null) {
            throw new SQLException("Base de données non connectée. Veuillez vérifier votre configuration MySQL.");
        }
    }

    @Override
    public void add(Cours cours) throws SQLException {
        checkConnection();
        String req = "INSERT INTO cours (titre, description, categorie, niveau, " +
                "duree, objectif_semaine, date_creation, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, cours.getTitre());
        pst.setString(2, cours.getDescription());
        pst.setString(3, cours.getCategorie());
        pst.setString(4, cours.getNiveau());
        pst.setString(5, cours.getDuree());
        pst.setString(6, cours.getObjectifSemaine());
        pst.setDate  (7, cours.getDateCreation() != null ? Date.valueOf(cours.getDateCreation()) : null);
        pst.setString(8, cours.getImageUrl());
        pst.executeUpdate();
        
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            cours.setIdCours(rs.getInt(1));
        }
    }

    @Override
    public void update(Cours cours) throws SQLException {
        checkConnection();
        String req = "UPDATE cours SET titre = ?, description = ?, categorie = ?, " +
                "niveau = ?, duree = ?, objectif_semaine = ?, " +
                "date_creation = ?, image_url = ? " +
                "WHERE id_cours = ?";

        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, cours.getTitre());
        pst.setString(2, cours.getDescription());
        pst.setString(3, cours.getCategorie());
        pst.setString(4, cours.getNiveau());
        pst.setString(5, cours.getDuree());
        pst.setString(6, cours.getObjectifSemaine());
        pst.setDate  (7, cours.getDateCreation() != null ? Date.valueOf(cours.getDateCreation()) : null);
        pst.setString(8, cours.getImageUrl());
        pst.setInt   (9, cours.getIdCours());
        pst.executeUpdate();
    }

    @Override
    public void delete(Cours cours) throws SQLException {
        checkConnection();
        String req = "DELETE FROM cours WHERE id_cours = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, cours.getIdCours());
        pst.executeUpdate();
    }

    @Override
    public List<Cours> getAll() throws SQLException {
        checkConnection();
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
        checkConnection();
        String req = "SELECT * FROM cours WHERE id_cours = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) return mapRow(rs);
        return null;
    }

    /**
     * @deprecated La progression a été supprimée des exigences du projet.
     */
    public void calculateAndSaveProgression(int coursId) throws SQLException {
        // Méthode conservée vide pour éviter les erreurs de compilation si elle est appelée ailleurs,
        // mais elle ne fait plus rien.
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

        Date dCreation = rs.getDate("date_creation");
        c.setDateCreation(dCreation != null ? dCreation.toLocalDate() : null);
        c.setImageUrl(rs.getString("image_url"));
        return c;
    }
}
