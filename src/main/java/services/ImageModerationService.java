package services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import utils.MyDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * ImageModerationService — Sightengine API integration for Skillora.
 *
 * DEV_MODE = true  → allow all images when API is not configured (development)
 * DEV_MODE = false → block all images when API is not configured (production safety)
 *
 * When API IS configured, real scores are always used regardless of DEV_MODE.
 * Network/API errors always block in production, allow in dev mode.
 */
public class ImageModerationService {

    // ── Configuration ──────────────────────────────────────────
    // Set to false in production
    private static final boolean DEV_MODE = true;

    // Sightengine credentials — replace with your real values
    private static final String API_USER   = "1648297771";
    private static final String API_SECRET = "7whEkWjDgcNKuNFiErqibhZia2XvBhDN";

    private static final String API_URL        = "https://api.sightengine.com/1.0/check.json";
    private static final double BLOCK_THRESHOLD = 0.70;

    // Placeholder sentinel — if user left the default value
    private static final String PLACEHOLDER_USER = "YOUR_SIGHTENGINE_API_USER";

    // ── HTTP client ────────────────────────────────────────────
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build();

    private static final Gson GSON = new Gson();

    // ═════════════════════════════════════════════════════════
    //  RESULT MODEL
    // ═════════════════════════════════════════════════════════

    public static class ImageModerationResult {
        public final boolean safe;
        public final String  reason;
        public final double  nudityScore;
        public final double  violenceScore;
        public final boolean weaponDetected;
        public final boolean spamDetected;

        ImageModerationResult(boolean safe, String reason,
                              double nudity, double violence,
                              boolean weapon, boolean spam) {
            this.safe           = safe;
            this.reason         = reason;
            this.nudityScore    = nudity;
            this.violenceScore  = violence;
            this.weaponDetected = weapon;
            this.spamDetected   = spam;
        }

        /** Safe result — image is clean */
        static ImageModerationResult allow() {
            return new ImageModerationResult(true, null, 0, 0, false, false);
        }

        /** Blocked result with reason */
        static ImageModerationResult block(String reason) {
            return new ImageModerationResult(false, reason, 0, 0, false, false);
        }

        @Override
        public String toString() {
            return String.format(
                "safe=%b | nudity=%.2f | violence=%.2f | weapon=%b | spam=%b | reason=%s",
                safe, nudityScore, violenceScore, weaponDetected, spamDetected, reason
            );
        }
    }

    // ═════════════════════════════════════════════════════════
    //  PUBLIC API
    // ═════════════════════════════════════════════════════════

    /**
     * Main entry point — call before accepting any image upload.
     *
     * @param imageFile the file chosen by the user
     * @param userId    uploader's id (for logging)
     */
    public static ImageModerationResult checkImage(File imageFile, int userId) {

        // ── Guard: file must exist ─────────────────────────────
        if (imageFile == null || !imageFile.exists()) {
            log("WARN", "File not found: " + imageFile);
            return ImageModerationResult.block("Fichier image introuvable.");
        }

        // ── Guard: credentials not configured ─────────────────
        boolean credentialsMissing = API_USER.equals(PLACEHOLDER_USER)
            || API_USER.isBlank()
            || API_SECRET.isBlank();

        if (credentialsMissing) {
            if (DEV_MODE) {
                log("DEV", "API credentials not configured — DEV_MODE=true, image allowed.");
                return ImageModerationResult.allow();
            } else {
                log("PROD", "API credentials not configured — DEV_MODE=false, image blocked.");
                return ImageModerationResult.block(
                    "Moderation API non configuree — image bloquee (mode production).");
            }
        }

        // ── Real API call ──────────────────────────────────────
        try {
            RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("media", imageFile.getName(),
                    RequestBody.create(imageFile, MediaType.parse("image/*")))
                .addFormDataPart("models", "nudity-2.1,weapon,violence,spam")
                .addFormDataPart("api_user",   API_USER)
                .addFormDataPart("api_secret", API_SECRET)
                .build();

            Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {

                if (!response.isSuccessful() || response.body() == null) {
                    log("ERROR", "API HTTP error: " + response.code());
                    return failSafe("Erreur API Sightengine (HTTP " + response.code() + ").");
                }

                String body = response.body().string();
                log("API", "Response: " + body);
                return parseResponse(body, imageFile.getName(), userId);
            }

        } catch (IOException e) {
            log("ERROR", "Network error: " + e.getMessage());
            return failSafe("Erreur reseau — moderation impossible.");
        }
    }

    // ═════════════════════════════════════════════════════════
    //  ADMIN STATISTICS
    // ═════════════════════════════════════════════════════════

    public static int getTotalBlockedImages() {
        try {
            java.sql.Connection cnx = MyDatabase.getInstance().getConnection();
            ResultSet rs = cnx.createStatement()
                .executeQuery("SELECT COUNT(*) FROM moderation_logs WHERE type='IMAGE'");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log("ERROR", "Stats query failed: " + e.getMessage());
        }
        return 0;
    }

    public static String getTopUnsafeUsers(int limit) {
        StringBuilder sb = new StringBuilder();
        try {
            java.sql.Connection cnx = MyDatabase.getInstance().getConnection();
            PreparedStatement pst = cnx.prepareStatement(
                "SELECT user_id, COUNT(*) as cnt FROM moderation_logs " +
                "WHERE type='IMAGE' GROUP BY user_id ORDER BY cnt DESC LIMIT ?"
            );
            pst.setInt(1, limit);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                sb.append("Utilisateur #").append(rs.getInt("user_id"))
                  .append(" — ").append(rs.getInt("cnt")).append(" violation(s)\n");
            }
        } catch (SQLException e) {
            log("ERROR", "Top users query failed: " + e.getMessage());
        }
        return sb.length() > 0 ? sb.toString() : "Aucune violation enregistree.";
    }

    // ═════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════

    /**
     * Parses the Sightengine JSON response.
     *
     * Sightengine nudity-2.1 response structure:
     * {
     *   "status": "success",
     *   "nudity": {
     *     "sexual_activity": 0.01,
     *     "sexual_display": 0.02,
     *     "erotica": 0.01,
     *     "suggestive": 0.05,
     *     "suggestive_classes": {...},
     *     "none": 0.97
     *   },
     *   "weapon": { "classes": [...] },
     *   "violence": { "prob": 0.01 },
     *   "type": { "spam": 0.01, ... }
     * }
     */
    private static ImageModerationResult parseResponse(String json, String filename, int userId) {
        try {
            JsonObject root = GSON.fromJson(json, JsonObject.class);

            // Check API-level status
            if (root.has("status") && "failure".equals(root.get("status").getAsString())) {
                String errMsg = root.has("error")
                    ? root.getAsJsonObject("error").get("message").getAsString()
                    : "API error";
                log("ERROR", "Sightengine API failure: " + errMsg);
                return failSafe("Erreur API: " + errMsg);
            }

            // ── Nudity score ───────────────────────────────────
            double nudityScore = 0.0;
            if (root.has("nudity")) {
                JsonObject nudity = root.getAsJsonObject("nudity");
                // Take the max of the explicit content scores
                nudityScore = max(
                    getDouble(nudity, "sexual_activity"),
                    getDouble(nudity, "sexual_display"),
                    getDouble(nudity, "erotica")
                );
                // "none" close to 1.0 means the image is clean — use as inverse check
                double noneScore = getDouble(nudity, "none");
                if (noneScore > 0.90) nudityScore = 0.0; // very likely safe
            }

            // ── Violence score ─────────────────────────────────
            double violenceScore = 0.0;
            if (root.has("violence")) {
                violenceScore = getDouble(root.getAsJsonObject("violence"), "prob");
            }

            // ── Weapon detection ──────────────────────────────
            boolean weaponDetected = false;
            if (root.has("weapon")) {
                JsonObject weapon = root.getAsJsonObject("weapon");
                // weapon.classes is an array of detected weapons
                if (weapon.has("classes")) {
                    weaponDetected = weapon.getAsJsonArray("classes").size() > 0;
                }
            }

            // ── Spam detection ────────────────────────────────
            boolean spamDetected = false;
            if (root.has("type")) {
                spamDetected = getDouble(root.getAsJsonObject("type"), "spam") >= BLOCK_THRESHOLD;
            }

            // ── Decision ──────────────────────────────────────
            String reason = null;
            if (nudityScore >= BLOCK_THRESHOLD) {
                reason = String.format("Contenu nudite/NSFW detecte (%.0f%%)", nudityScore * 100);
            } else if (violenceScore >= BLOCK_THRESHOLD) {
                reason = String.format("Contenu violent detecte (%.0f%%)", violenceScore * 100);
            } else if (weaponDetected) {
                reason = "Arme detectee dans l'image.";
            } else if (spamDetected) {
                reason = "Image spam/watermark detectee.";
            }

            boolean safe = (reason == null);
            log("RESULT", filename + " → " + (safe ? "SAFE" : "BLOCKED: " + reason));

            if (!safe) logBlockedImage(userId, filename, reason, nudityScore, violenceScore);

            return new ImageModerationResult(safe, reason,
                nudityScore, violenceScore, weaponDetected, spamDetected);

        } catch (Exception e) {
            log("ERROR", "JSON parse error: " + e.getMessage());
            return failSafe("Erreur analyse reponse API.");
        }
    }

    /**
     * Decides what to do on API/network failure based on DEV_MODE.
     * DEV_MODE=true  → allow (don't block users during development)
     * DEV_MODE=false → block (production safety)
     */
    private static ImageModerationResult failSafe(String reason) {
        if (DEV_MODE) {
            log("DEV", "API failure — DEV_MODE=true, image allowed. Reason: " + reason);
            return ImageModerationResult.allow();
        } else {
            log("PROD", "API failure — DEV_MODE=false, image blocked. Reason: " + reason);
            return ImageModerationResult.block(reason);
        }
    }

    private static void logBlockedImage(int userId, String filename,
                                        String reason, double nudity, double violence) {
        try {
            java.sql.Connection cnx = MyDatabase.getInstance().getConnection();
            cnx.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS moderation_logs (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  type VARCHAR(20) NOT NULL DEFAULT 'IMAGE'," +
                "  user_id INT NOT NULL," +
                "  filename VARCHAR(255)," +
                "  reason TEXT," +
                "  nudity_score DOUBLE DEFAULT 0," +
                "  violence_score DOUBLE DEFAULT 0," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            PreparedStatement pst = cnx.prepareStatement(
                "INSERT INTO moderation_logs " +
                "(type, user_id, filename, reason, nudity_score, violence_score) " +
                "VALUES ('IMAGE', ?, ?, ?, ?, ?)"
            );
            pst.setInt(1, userId);
            pst.setString(2, filename);
            pst.setString(3, reason);
            pst.setDouble(4, nudity);
            pst.setDouble(5, violence);
            pst.executeUpdate();
            log("DB", "Blocked image logged for user #" + userId);
        } catch (SQLException e) {
            log("ERROR", "DB log failed: " + e.getMessage());
        }
    }

    private static double getDouble(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) return 0.0;
        JsonElement el = obj.get(key);
        if (el.isJsonNull()) return 0.0;
        try { return el.getAsDouble(); } catch (Exception e) { return 0.0; }
    }

    private static double max(double... values) {
        double m = 0.0;
        for (double v : values) if (v > m) m = v;
        return m;
    }

    private static void log(String level, String msg) {
        System.out.println("[ImageModeration][" + level + "] " + msg);
    }
}
