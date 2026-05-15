package services;

import entities.Badge;
import utils.MyDatabase;

import java.sql.*;

/**
 * BadgeService — Internal badge and score system for Skillora.
 *
 * No external API or library required.
 *
 * Score rules:
 *   POST_CREATED   → +1 point  (user creates a post)
 *   COMMENT_ADDED  → +2 points (user writes a comment)
 *   LIKE_RECEIVED  → +3 points (user's post receives a like)
 *
 * Badge thresholds (auto-calculated from score):
 *   🟢 Debutant : score  0 – 9
 *   🔵 Actif    : score 10 – 30
 *   🔴 Pro      : score > 30
 *
 * Integration:
 *   Call BadgeService.onPostCreated(userId)   after saving a post
 *   Call BadgeService.onCommentAdded(userId)  after saving a comment
 *   Call BadgeService.onLikeReceived(userId)  after a like is added to a post
 *
 * The service auto-creates the score/badge columns if they don't exist yet.
 */
public class BadgeService {

    // ── Point values ───────────────────────────────────────────
    public static final int POINTS_POST_CREATED  = 1;
    public static final int POINTS_COMMENT_ADDED = 2;
    public static final int POINTS_LIKE_RECEIVED = 3;

    // ── Singleton connection ───────────────────────────────────
    private static final java.sql.Connection CNX =
        MyDatabase.getInstance().getConnection();

    // Run migration once when class is loaded
    static {
        migrate();
    }

    // ═════════════════════════════════════════════════════════
    //  PUBLIC TRIGGER METHODS
    //  Call these from PosteService / CommentaireService / JaimeService
    // ═════════════════════════════════════════════════════════

    /**
     * Call after a user successfully creates a post.
     * Awards +1 point to the post author.
     */
    public static void onPostCreated(int userId) {
        addPoints(userId, POINTS_POST_CREATED, "post created");
    }

    /**
     * Call after a user successfully writes a comment.
     * Awards +2 points to the comment author.
     */
    public static void onCommentAdded(int userId) {
        addPoints(userId, POINTS_COMMENT_ADDED, "comment added");
    }

    /**
     * Call after a like is added to a post.
     * Awards +3 points to the POST OWNER (not the liker).
     *
     * @param postOwnerId the id of the user who owns the liked post
     */
    public static void onLikeReceived(int postOwnerId) {
        addPoints(postOwnerId, POINTS_LIKE_RECEIVED, "like received");
    }

    // ═════════════════════════════════════════════════════════
    //  READ METHODS
    // ═════════════════════════════════════════════════════════

    /**
     * Returns the current score for a user.
     */
    public static int getScore(int userId) {
        try {
            PreparedStatement pst = CNX.prepareStatement(
                "SELECT score FROM utilisateurs WHERE id_utilisateur = ?");
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("score");
        } catch (SQLException e) {
            log("ERROR", "getScore failed for user #" + userId + ": " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns the current badge for a user.
     */
    public static Badge getBadge(int userId) {
        return Badge.fromScore(getScore(userId));
    }

    /**
     * Returns the badge display string for a user, e.g. "🔵 Actif".
     */
    public static String getBadgeDisplay(int userId) {
        return getBadge(userId).display();
    }

    // ═════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════

    /**
     * Adds points to a user's score and recalculates their badge.
     * Both operations are done in a single UPDATE for atomicity.
     */
    private static void addPoints(int userId, int points, String reason) {
        try {
            // Increment score and recalculate badge in one query
            PreparedStatement pst = CNX.prepareStatement(
                "UPDATE utilisateurs " +
                "SET score = score + ?, " +
                "    badge = CASE " +
                "              WHEN score + ? >= 31 THEN 'PRO' " +
                "              WHEN score + ? >= 10 THEN 'ACTIF' " +
                "              ELSE 'DEBUTANT' " +
                "            END " +
                "WHERE id_utilisateur = ?"
            );
            pst.setInt(1, points);
            pst.setInt(2, points);
            pst.setInt(3, points);
            pst.setInt(4, userId);
            pst.executeUpdate();

            // Log the new state
            int newScore = getScore(userId);
            log("SCORE", "User #" + userId + " +" + points + " pts (" + reason + ")"
                + " → score=" + newScore + " badge=" + Badge.fromScore(newScore).display());

        } catch (SQLException e) {
            log("ERROR", "addPoints failed for user #" + userId + ": " + e.getMessage());
        }
    }

    /**
     * Auto-migration: adds score and badge columns to utilisateurs table
     * if they don't already exist. Safe to call multiple times.
     */
    private static void migrate() {
        try {
            DatabaseMetaData meta = CNX.getMetaData();

            // Add score column if missing
            ResultSet scoreCol = meta.getColumns(null, null, "utilisateurs", "score");
            if (!scoreCol.next()) {
                CNX.createStatement().executeUpdate(
                    "ALTER TABLE utilisateurs ADD COLUMN score INT NOT NULL DEFAULT 0"
                );
                log("MIGRATE", "Added column: utilisateurs.score");
            }

            // Add badge column if missing
            ResultSet badgeCol = meta.getColumns(null, null, "utilisateurs", "badge");
            if (!badgeCol.next()) {
                CNX.createStatement().executeUpdate(
                    "ALTER TABLE utilisateurs " +
                    "ADD COLUMN badge ENUM('DEBUTANT','ACTIF','PRO') " +
                    "NOT NULL DEFAULT 'DEBUTANT'"
                );
                log("MIGRATE", "Added column: utilisateurs.badge");
            }

        } catch (SQLException e) {
            log("ERROR", "Migration failed: " + e.getMessage());
        }
    }

    private static void log(String level, String msg) {
        System.out.println("[BadgeService][" + level + "] " + msg);
    }
}
