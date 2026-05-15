package services;

import entities.Commentaire;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    private final Connection connection;

    public CommentaireService() {
        connection = MyDatabase.getInstance().getConnection();
        ensureParentIdColumn();
    }

    // ── Auto-migrate: add parent_id if missing ─────────────────
    private void ensureParentIdColumn() {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet cols = meta.getColumns(null, null, "commentaire", "parent_id");
            if (!cols.next()) {
                Statement st = connection.createStatement();
                st.executeUpdate(
                    "ALTER TABLE commentaire ADD COLUMN parent_id INT NULL DEFAULT NULL, " +
                    "ADD CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) " +
                    "REFERENCES commentaire(id_commentaire) ON DELETE CASCADE"
                );
                System.out.println("Migration: parent_id column added to commentaire.");
            }
        } catch (SQLException e) {
            System.err.println("Migration warning: " + e.getMessage());
        }
    }

    // ── CRUD ───────────────────────────────────────────────────

    public void add(Commentaire commentaire) throws SQLException {
        String req = "INSERT INTO `commentaire`(`contenu`, `id_utilisateur`, `id_post`, `parent_id`) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, commentaire.getContenu());
        pst.setInt(2, commentaire.getIdUtilisateur());
        pst.setInt(3, commentaire.getIdPost());
        if (commentaire.getParentId() != null) {
            pst.setInt(4, commentaire.getParentId());
        } else {
            pst.setNull(4, Types.INTEGER);
        }
        pst.executeUpdate();
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            commentaire.setIdCommentaire(rs.getInt(1));
        }
    }

    public void update(Commentaire commentaire) throws SQLException {
        String req = "UPDATE commentaire SET contenu = ? WHERE id_commentaire = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setString(1, commentaire.getContenu());
        pst.setInt(2, commentaire.getIdCommentaire());
        pst.executeUpdate();
    }

    public void delete(Commentaire commentaire) throws SQLException {
        String req = "DELETE FROM commentaire WHERE id_commentaire = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, commentaire.getIdCommentaire());
        pst.executeUpdate();
    }

    public void delete(int idCommentaire) throws SQLException {
        String req = "DELETE FROM commentaire WHERE id_commentaire = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, idCommentaire);
        pst.executeUpdate();
    }

    // ── Queries ────────────────────────────────────────────────

    public List<Commentaire> getAll() throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM commentaire");
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    /** Top-level comments only (parent_id IS NULL) */
    public List<Commentaire> getByPost(int idPost) throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        String req = "SELECT * FROM commentaire WHERE id_post = ? AND parent_id IS NULL ORDER BY date_creation ASC";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, idPost);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    /** Direct replies to a parent comment */
    public List<Commentaire> getReplies(int parentId) throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        String req = "SELECT * FROM commentaire WHERE parent_id = ? ORDER BY date_creation ASC";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, parentId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    /** Total comment count (top-level + replies) for a post */
    public int countByPost(int idPost) throws SQLException {
        String req = "SELECT COUNT(*) FROM commentaire WHERE id_post = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, idPost);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    // ── Mapper ─────────────────────────────────────────────────

    private Commentaire mapRow(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setIdCommentaire(rs.getInt("id_commentaire"));
        c.setContenu(rs.getString("contenu"));
        c.setDateCreation(rs.getTimestamp("date_creation"));
        c.setIdUtilisateur(rs.getInt("id_utilisateur"));
        c.setIdPost(rs.getInt("id_post"));
        int parentId = rs.getInt("parent_id");
        if (!rs.wasNull()) c.setParentId(parentId);
        return c;
    }
}
