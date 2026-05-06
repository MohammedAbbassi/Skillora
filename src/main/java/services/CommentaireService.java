package services;

import entities.Commentaire;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {
    private Connection conn = MyDatabase.getInstance().getConnection();

    public void add(Commentaire c) throws SQLException {
        String sql = "INSERT INTO commentaire (contenu, id_utilisateur, id_post) VALUES (?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, c.getContenu());
        ps.setInt(2, c.getIdUtilisateur());
        ps.setInt(3, c.getIdPost());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) c.setIdCommentaire(rs.getInt(1));
    }

    public void update(Commentaire c) throws SQLException {
        String sql = "UPDATE commentaire SET contenu=? WHERE id_commentaire=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, c.getContenu());
        ps.setInt(2, c.getIdCommentaire());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM commentaire WHERE id_commentaire=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Commentaire> getByPost(int postId) throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM commentaire WHERE id_post=? ORDER BY date_creation ASC");
        ps.setInt(1, postId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setIdCommentaire(rs.getInt("id_commentaire"));
            c.setContenu(rs.getString("contenu"));
            c.setIdUtilisateur(rs.getInt("id_utilisateur"));
            c.setIdPost(rs.getInt("id_post"));
            c.setDateCreation(rs.getTimestamp("date_creation"));
            list.add(c);
        }
        return list;
    }
}
