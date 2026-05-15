package services;

import entities.Jaime;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JaimeService {

    private Connection connection;

    public JaimeService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public void add(Jaime jaime) throws SQLException {
        String req = "INSERT INTO `jaime`(`id_post`, `id_utilisateur`) VALUES (?, ?)";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, jaime.getIdPost());
        pst.setInt(2, jaime.getIdUtilisateur());
        pst.executeUpdate();
    }

    public void delete(Jaime jaime) throws SQLException {
        String req = "DELETE FROM jaime WHERE id_post = ? AND id_utilisateur = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, jaime.getIdPost());
        pst.setInt(2, jaime.getIdUtilisateur());
        pst.executeUpdate();
    }

    public List<Jaime> getAll() throws SQLException {
        List<Jaime> jaimes = new ArrayList<>();
        String req = "SELECT * FROM jaime";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Jaime jaime = new Jaime();
            jaime.setIdPost(rs.getInt("id_post"));
            jaime.setIdUtilisateur(rs.getInt("id_utilisateur"));
            jaime.setDateJaime(rs.getTimestamp("date_jaime"));
            jaimes.add(jaime);
        }
        return jaimes;
    }

    public int getCount(int idPost) throws SQLException {
        String req = "SELECT COUNT(*) FROM jaime WHERE id_post = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, idPost);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    /** Alias for getCount — used by some callers */
    public int getCountByPost(int idPost) throws SQLException {
        return getCount(idPost);
    }

    public boolean hasLiked(int userId, int postId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT 1 FROM jaime WHERE id_utilisateur=? AND id_post=?");
        ps.setInt(1, userId);
        ps.setInt(2, postId);
        return ps.executeQuery().next();
    }

    public boolean toggle(int userId, int postId) throws SQLException {
        if (hasLiked(userId, postId)) {
            PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM jaime WHERE id_utilisateur=? AND id_post=?");
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            ps.executeUpdate();
            return false; // unliked
        } else {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO jaime (id_utilisateur, id_post) VALUES (?,?)");
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            ps.executeUpdate();
            return true; // liked
        }
    }
}
