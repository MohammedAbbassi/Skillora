package com.skillora.shop.services;

import com.skillora.shop.entities.CategorieProduit;
import com.skillora.shop.entities.Produit;
import com.skillora.shop.interfaces.InterfaceCRUD;
import com.skillora.shop.utils.MyDatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProduitCRUD implements InterfaceCRUD<Produit> {

    private Boolean hasLangue;
    private Boolean hasNiveau;
    private Boolean hasImage;
    private Boolean hasIdCours;

    private Connection connection() throws SQLException {
        Connection conn = MyDatabase.getInstance().getCnx();
        if (conn == null) {
            throw new SQLException("Connexion base de donnees indisponible. Verifiez que MySQL est lance et que la base skillora est importee.");
        }
        return conn;
    }

    @Override
    public void ajouter(Produit p) throws SQLException {
        Connection conn = connection();
        List<String> columns = new ArrayList<>(List.of("nom", "prix", "categorie", "description"));
        List<Object> values = new ArrayList<>(List.of(
                p.getNom(),
                p.getPrix(),
                p.getCategorie() == null ? null : p.getCategorie().name(),
                p.getDescription()
        ));

        addOptionalProductFields(conn, columns, values, p);

        String placeholders = String.join(", ", java.util.Collections.nCopies(columns.size(), "?"));
        String sql = "INSERT INTO produit (" + String.join(", ", columns) + ") VALUES (" + placeholders + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindValues(ps, values);
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Produit p) throws SQLException {
        Connection conn = connection();
        List<String> assignments = new ArrayList<>(List.of("nom=?", "prix=?", "categorie=?", "description=?"));
        List<Object> values = new ArrayList<>(List.of(
                p.getNom(),
                p.getPrix(),
                p.getCategorie() == null ? null : p.getCategorie().name(),
                p.getDescription()
        ));

        addOptionalProductAssignments(conn, assignments, values, p);
        values.add(p.getId());

        String sql = "UPDATE produit SET " + String.join(", ", assignments) + " WHERE id_produit=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindValues(ps, values);
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
        Connection conn = connection();
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT p.*, COALESCE(AVG(e.note), 0) as note_moyenne, COUNT(e.id_evaluation) as nb_evals " +
                     "FROM produit p " +
                     "LEFT JOIN evaluation e ON p.id_produit = e.id_produit " +
                     "GROUP BY p.id_produit " +
                     "ORDER BY p.nom";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            boolean langue = hasLangue(conn);
            boolean niveau = hasNiveau(conn);
            boolean image = hasImage(conn);
            boolean idCours = hasIdCours(conn);

            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getLong("id_produit"));
                p.setNom(rs.getString("nom"));
                p.setPrix(rs.getDouble("prix"));
                String cat = rs.getString("categorie");
                if (cat != null && !cat.isBlank()) {
                    p.setCategorie(CategorieProduit.valueOf(cat));
                }
                p.setDescription(rs.getString("description"));
                if (langue) {
                    p.setLangue(rs.getString("langue"));
                }
                if (niveau) {
                    p.setNiveau(rs.getString("niveau"));
                }
                if (image) {
                    p.setImage(rs.getString("image"));
                }
                if (idCours) {
                    int ic = rs.getInt("id_cours");
                    if (!rs.wasNull()) {
                        p.setIdCours(ic);
                    }
                }
                p.setNoteMoyenne(rs.getDouble("note_moyenne"));
                p.setNombreEvaluations(rs.getInt("nb_evals"));
                list.add(p);
            }
        }
        return list;
    }

    private void addOptionalProductFields(Connection conn, List<String> columns, List<Object> values, Produit p) throws SQLException {
        if (hasLangue(conn)) {
            columns.add("langue");
            values.add(p.getLangue());
        }
        if (hasNiveau(conn)) {
            columns.add("niveau");
            values.add(p.getNiveau());
        }
        if (hasImage(conn)) {
            columns.add("image");
            values.add(p.getImage());
        }
        if (hasIdCours(conn)) {
            columns.add("id_cours");
            values.add(p.getIdCours());
        }
    }

    private void addOptionalProductAssignments(Connection conn, List<String> assignments, List<Object> values, Produit p) throws SQLException {
        if (hasLangue(conn)) {
            assignments.add("langue=?");
            values.add(p.getLangue());
        }
        if (hasNiveau(conn)) {
            assignments.add("niveau=?");
            values.add(p.getNiveau());
        }
        if (hasImage(conn)) {
            assignments.add("image=?");
            values.add(p.getImage());
        }
        if (hasIdCours(conn)) {
            assignments.add("id_cours=?");
            values.add(p.getIdCours());
        }
    }

    private void bindValues(PreparedStatement ps, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            ps.setObject(i + 1, values.get(i));
        }
    }

    private boolean hasLangue(Connection conn) throws SQLException {
        if (hasLangue == null) {
            hasLangue = hasColumn(conn, "langue");
        }
        return hasLangue;
    }

    private boolean hasNiveau(Connection conn) throws SQLException {
        if (hasNiveau == null) {
            hasNiveau = hasColumn(conn, "niveau");
        }
        return hasNiveau;
    }

    private boolean hasImage(Connection conn) throws SQLException {
        if (hasImage == null) {
            hasImage = hasColumn(conn, "image");
        }
        return hasImage;
    }

    private boolean hasIdCours(Connection conn) throws SQLException {
        if (hasIdCours == null) {
            hasIdCours = hasColumn(conn, "id_cours");
        }
        return hasIdCours;
    }

    private boolean hasColumn(Connection conn, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, "produit", columnName)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, "produit", columnName.toUpperCase())) {
            return rs.next();
        }
    }
}
