package services;

import utils.MyDatabase;

import java.sql.*;

public class JaimeService {
    private Connection conn = MyDatabase.getInstance().getConnection();

    public boolean hasLiked(int userId, int postId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT 1 FROM jaime WHERE id_utilisateur=? AND id_post=?");
        ps.setInt(1, userId);
        ps.setInt(2, postId);
        return ps.executeQuery().next();
    }

    public boolean toggle(int userId, int postId) throws SQLException {
        if (hasLiked(userId, postId)) {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM jaime WHERE id_utilisateur=? AND id_post=?");
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            ps.executeUpdate();
            return false; // unliked
        } else {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO jaime (id_utilisateur, id_post) VALUES (?,?)");
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            ps.executeUpdate();
            return true; // liked
        }
    }

    public int getCount(int postId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT COUNT(*) FROM jaime WHERE id_post=?");
        ps.setInt(1, postId);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }
}
