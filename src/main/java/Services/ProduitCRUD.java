package Services;

import Entities.CategorieProduit;
import Entities.Produit;
import Interfaces.InterfaceCRUD;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitCRUD implements InterfaceCRUD<Produit> {

    private final Connection conn = MyDatabase.getInstance().getCnx();

    @Override
    public void ajouter(Produit p) throws SQLException {
        String sql = "INSERT INTO produit (nom, prix, langue, categorie, description, niveau) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, p.getNom());
        ps.setDouble(2, p.getPrix());
        ps.setString(3, p.getLangue());
        ps.setString(4, p.getCategorie() == null ? null : p.getCategorie().name());
        ps.setString(5, p.getDescription());
        ps.setString(6, p.getNiveau());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Produit p) throws SQLException {
        String sql = "UPDATE produit SET nom=?, prix=?, langue=?, categorie=?, description=?, niveau=? WHERE id_produit=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, p.getNom());
        ps.setDouble(2, p.getPrix());
        ps.setString(3, p.getLangue());
        ps.setString(4, p.getCategorie() == null ? null : p.getCategorie().name());
        ps.setString(5, p.getDescription());
        ps.setString(6, p.getNiveau());
        ps.setLong(7, p.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM produit WHERE id_produit=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Produit> afficher() throws SQLException {
        List<Produit> list = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM produit ORDER BY nom")) {
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
