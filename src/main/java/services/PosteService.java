package services;

import entities.Poste;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PosteService {

    private Connection connection;

    public PosteService() {
        connection = MyDatabase.getInstance().getConnection();
        migrateRepostColumn();
    }

    /**
     * Auto-adds original_post_id column to post table if it doesn't exist yet.
     * Safe to call on every startup.
     */
    private void migrateRepostColumn() {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet col = meta.getColumns(null, null, "post", "original_post_id");
            if (!col.next()) {
                connection.createStatement().executeUpdate(
                    "ALTER TABLE post ADD COLUMN original_post_id INT NULL DEFAULT NULL, " +
                    "ADD CONSTRAINT fk_repost FOREIGN KEY (original_post_id) " +
                    "REFERENCES post(id_post) ON DELETE SET NULL"
                );
                System.out.println("[PosteService] Migration: original_post_id column added.");
            }
        } catch (SQLException e) {
            System.err.println("[PosteService] Migration warning: " + e.getMessage());
        }
    }

    public void add(Poste poste) throws SQLException {
        String req = "INSERT INTO `post`(`titre`, `contenu`, `image`, `id_utilisateur`, `original_post_id`) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, poste.getTitre());
        pst.setString(2, poste.getContenu());
        pst.setString(3, poste.getImage());
        pst.setInt(4, poste.getIdUtilisateur());
        if (poste.getOriginalPostId() != null) {
            pst.setInt(5, poste.getOriginalPostId());
        } else {
            pst.setNull(5, Types.INTEGER);
        }
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            poste.setIdPost(rs.getInt(1));
        }
        // Award +1 point to the post author
        BadgeService.onPostCreated(poste.getIdUtilisateur());
    }

    public void update(Poste poste) throws SQLException {
        String req = "UPDATE post SET titre = ?, contenu = ?, image = ? WHERE id_post = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setString(1, poste.getTitre());
        pst.setString(2, poste.getContenu());
        pst.setString(3, poste.getImage());
        pst.setInt(4, poste.getIdPost());
        pst.executeUpdate();
    }

    public void delete(Poste poste) throws SQLException {
        String req = "DELETE FROM post WHERE id_post = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, poste.getIdPost());
        pst.executeUpdate();
    }

    public List<Poste> getAll() throws SQLException {
        List<Poste> postes = new ArrayList<>();
        String req = "SELECT * FROM post ORDER BY date_creation DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            postes.add(mapRow(rs));
        }
        return postes;
    }

    public Poste getById(int id) throws SQLException {
        String req = "SELECT * FROM post WHERE id_post = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapRow(rs) : null;
    }

    /**
     * Creates a repost: copies content from original post, sets original_post_id.
     * Prevents duplicate repost by the same user on the same post.
     *
     * @param originalPostId  id of the post being reposted
     * @param repostingUserId id of the user doing the repost
     * @return the new repost Poste, or null if already reposted
     */
    public Poste repost(int originalPostId, int repostingUserId) throws SQLException {
        // Prevent duplicate repost
        if (hasReposted(originalPostId, repostingUserId)) {
            return null; // already reposted
        }
        Poste original = getById(originalPostId);
        if (original == null) return null;

        Poste repost = new Poste(
            original.getTitre(),
            original.getContenu(),
            original.getImage(),
            repostingUserId
        );
        repost.setOriginalPostId(originalPostId);
        add(repost);
        return repost;
    }

    /**
     * Returns true if userId has already reposted originalPostId.
     */
    public boolean hasReposted(int originalPostId, int userId) throws SQLException {
        PreparedStatement pst = connection.prepareStatement(
            "SELECT 1 FROM post WHERE original_post_id = ? AND id_utilisateur = ?");
        pst.setInt(1, originalPostId);
        pst.setInt(2, userId);
        return pst.executeQuery().next();
    }

    /**
     * Returns how many times a post has been reposted.
     */
    public int getRepostCount(int originalPostId) throws SQLException {
        PreparedStatement pst = connection.prepareStatement(
            "SELECT COUNT(*) FROM post WHERE original_post_id = ?");
        pst.setInt(1, originalPostId);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    /** Maps a ResultSet row to a Poste, including original_post_id. */
    private Poste mapRow(ResultSet rs) throws SQLException {
        Poste poste = new Poste();
        poste.setIdPost(rs.getInt("id_post"));
        poste.setTitre(rs.getString("titre"));
        poste.setContenu(rs.getString("contenu"));
        poste.setImage(rs.getString("image"));
        poste.setDateCreation(rs.getTimestamp("date_creation"));
        poste.setIdUtilisateur(rs.getInt("id_utilisateur"));
        int origId = rs.getInt("original_post_id");
        if (!rs.wasNull()) poste.setOriginalPostId(origId);
        return poste;
    }

    /**
     * Searches posts by keyword — case-insensitive, looks in titre AND contenu.
     */
    public List<Poste> search(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) return getAll();
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        String req = "SELECT * FROM post " +
                     "WHERE LOWER(titre) LIKE ? OR LOWER(contenu) LIKE ? " +
                     "ORDER BY date_creation DESC";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setString(1, pattern);
        pst.setString(2, pattern);
        ResultSet rs = pst.executeQuery();
        List<Poste> results = new ArrayList<>();
        while (rs.next()) results.add(mapRow(rs));
        return results;
    }
}
