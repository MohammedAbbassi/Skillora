package com.skillora.shop.services;

import com.skillora.shop.entities.CategorieProduit;
import com.skillora.shop.entities.Produit;
import com.skillora.shop.interfaces.InterfaceCRUD;
import com.skillora.shop.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProduitCRUD implements InterfaceCRUD<Produit> {

    private Connection connection() throws SQLException {
        Connection conn = MyDatabase.getInstance().getCnx();
        if (conn == null) {
            throw new SQLException("Connexion base de donnees indisponible. Verifiez que MySQL est lance et que la base skillora_shop est importee.");
        }
        return conn;
    }

    @Override
    public void ajouter(Produit p) throws SQLException {
        String sql = "INSERT INTO produit (nom, prix, langue, categorie, description, niveau, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getPrix());
            ps.setString(3, p.getLangue());
            ps.setString(4, p.getCategorie() == null ? null : p.getCategorie().name());
            ps.setString(5, p.getDescription());
            ps.setString(6, p.getNiveau());
            ps.setString(7, p.getImage());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Produit p) throws SQLException {
        String sql = "UPDATE produit SET nom=?, prix=?, langue=?, categorie=?, description=?, niveau=?, image=? WHERE id_produit=?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getPrix());
            ps.setString(3, p.getLangue());
            ps.setString(4, p.getCategorie() == null ? null : p.getCategorie().name());
            ps.setString(5, p.getDescription());
            ps.setString(6, p.getNiveau());
            ps.setString(7, p.getImage());
            ps.setLong(8, p.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = connection().prepareStatement("DELETE FROM produit WHERE id_produit=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Produit> afficher() throws SQLException {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT p.*, COALESCE(AVG(e.note), 0) as note_moyenne, COUNT(e.id_evaluation) as nb_evals " +
                     "FROM produit p " +
                     "LEFT JOIN evaluation e ON p.id_produit = e.id_produit " +
                     "GROUP BY p.id_produit " +
                     "ORDER BY p.nom";
        try (Statement st = connection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getLong("id_produit"));
                p.setNom(rs.getString("nom"));
                p.setPrix(rs.getDouble("prix"));
                p.setLangue(rs.getString("langue"));
                String cat = rs.getString("categorie");
                if (cat != null && !cat.isBlank()) {
                    p.setCategorie(CategorieProduit.valueOf(cat));
                }
                p.setDescription(rs.getString("description"));
                p.setNiveau(rs.getString("niveau"));
                p.setImage(rs.getString("image"));
                p.setNoteMoyenne(rs.getDouble("note_moyenne"));
                p.setNombreEvaluations(rs.getInt("nb_evals"));
                int ic = rs.getInt("id_cours");
                if (!rs.wasNull()) {
                    p.setIdCours(ic);
                }
                list.add(p);
            }
        }
        return list;
    }
}
