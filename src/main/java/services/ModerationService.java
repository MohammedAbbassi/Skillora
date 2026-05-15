package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ModerationService — Google Perspective API + local filters for Skillora.
 *
 * DEV_MODE = true  → API errors / missing key allow content (development)
 * DEV_MODE = false → API errors / missing key block content (production safety)
 *
 * Local filters (banned words, spam, caps) always apply regardless of DEV_MODE.
 */
public class ModerationService {

    // ── Configuration ──────────────────────────────────────────
    // Set to false in production
    private static final boolean DEV_MODE = true;

    // Replace with your real key from https://perspectiveapi.com/
    private static final String API_KEY = "YOUR_PERSPECTIVE_API_KEY";
    private static final String API_URL =
        "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + API_KEY;

    private static final double TOXICITY_THRESHOLD = 0.70;
    private static final String PLACEHOLDER_KEY    = "YOUR_PERSPECTIVE_API_KEY";

    // ── HTTP client ────────────────────────────────────────────
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build();

    private static final Gson GSON = new Gson();

    // ── Banned words ───────────────────────────────────────────
    private static final Set<String> BANNED_WORDS = new HashSet<>(Arrays.asList(
        "spam", "idiot", "stupid", "hate", "kill", "die",
        "insulte", "connard", "salaud", "merde", "putain",
        "raciste", "nazi", "terroriste"
    ));

    // ── In-memory state ────────────────────────────────────────
    private static final Map<Integer, Integer> USER_WARNINGS = new ConcurrentHashMap<>();
    private static final Map<Integer, String>  LAST_CONTENT  = new ConcurrentHashMap<>();

    private static int totalChecks   = 0;
    private static int totalBlocked  = 0;
    private static int totalWarnings = 0;

    // ═════════════════════════════════════════════════════════
    //  RESULT MODEL
    // ═════════════════════════════════════════════════════════

    public static class ModerationResult {
        public final boolean allowed;
        public final String  reason;
        public final double  toxicityScore;
        public final int     warnings;

        ModerationResult(boolean allowed, String reason, double score, int warnings) {
            this.allowed       = allowed;
            this.reason        = reason;
            this.toxicityScore = score;
            this.warnings      = warnings;
        }
    }

    // ═════════════════════════════════════════════════════════
    //  PUBLIC API
    // ═════════════════════════════════════════════════════════

    /**
     * Analyzes text before publishing a post or comment.
     *
     * Order of checks:
     *  1. Banned words  (local, instant)
     *  2. Spam / caps   (local, instant)
     *  3. Perspective API (network, only if key is configured)
     */
    public static ModerationResult analyze(String text, int userId) {
        totalChecks++;

        // ── 1. Banned words ────────────────────────────────────
        String lower = text.toLowerCase();
        for (String word : BANNED_WORDS) {
            if (lower.contains(word)) {
                log("BANNED", "Word \"" + word + "\" found in content by user #" + userId);
                return block(userId, "Mot interdit detecte: \"" + word + "\"", 0.0);
            }
        }

        // ── 2. Spam detection ──────────────────────────────────
        String lastText = LAST_CONTENT.get(userId);
        if (text.equals(lastText)) {
            log("SPAM", "Duplicate content from user #" + userId);
            return block(userId, "Contenu duplique detecte (spam).", 0.0);
        }
        if (isExcessiveCaps(text)) {
            log("CAPS", "Excessive caps from user #" + userId);
            return block(userId, "Contenu en majuscules excessives detecte.", 0.0);
        }

        // ── 3. Perspective API ─────────────────────────────────
        double score = callPerspectiveApi(text, userId);
        LAST_CONTENT.put(userId, text);

        if (score >= TOXICITY_THRESHOLD) {
            return block(userId,
                String.format("Contenu inapproprie detecte (score: %.0f%%).", score * 100),
                score);
        }

        // Content is clean
        return new ModerationResult(true, null, score, getUserWarnings(userId));
    }

    // ═════════════════════════════════════════════════════════
    //  ADMIN STATISTICS
    // ═════════════════════════════════════════════════════════

    public static String getAdminStats() {
        return String.format(
            "Moderations: %d verifications | %d bloques | %d avertissements",
            totalChecks, totalBlocked, totalWarnings
        );
    }

    public static int getTotalChecks()        { return totalChecks;   }
    public static int getTotalBlocked()       { return totalBlocked;  }
    public static int getTotalWarnings()      { return totalWarnings; }
    public static int getUserWarnings(int id) { return USER_WARNINGS.getOrDefault(id, 0); }

    public static void resetStats() {
        totalChecks = 0; totalBlocked = 0; totalWarnings = 0;
        USER_WARNINGS.clear(); LAST_CONTENT.clear();
    }

    // ═════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════

    /**
     * Calls Perspective API and returns the highest attribute score (0.0–1.0).
     *
     * Returns 0.0 (allow) when:
     *  - API key is not configured AND DEV_MODE=true
     *
     * Returns 1.0 (block) when:
     *  - API key is not configured AND DEV_MODE=false
     *  - Network/API error AND DEV_MODE=false
     *
     * Returns 0.0 (allow) on error when DEV_MODE=true.
     */
    private static double callPerspectiveApi(String text, int userId) {
        // Key not configured
        if (API_KEY.equals(PLACEHOLDER_KEY) || API_KEY.isBlank()) {
            if (DEV_MODE) {
                log("DEV", "Perspective API key not configured — DEV_MODE=true, skipping API check.");
                return 0.0; // allow
            } else {
                log("PROD", "Perspective API key not configured — DEV_MODE=false, blocking content.");
                return 1.0; // block
            }
        }

        // Build request
        JsonObject comment = new JsonObject();
        comment.addProperty("text", text);

        JsonObject requestedAttributes = new JsonObject();
        requestedAttributes.add("TOXICITY",       new JsonObject());
        requestedAttributes.add("INSULT",          new JsonObject());
        requestedAttributes.add("THREAT",          new JsonObject());
        requestedAttributes.add("IDENTITY_ATTACK", new JsonObject());
        requestedAttributes.add("SEVERE_TOXICITY", new JsonObject());

        JsonObject body = new JsonObject();
        body.add("comment", comment);
        body.add("requestedAttributes", requestedAttributes);
        body.addProperty("languages", "fr");

        RequestBody requestBody = RequestBody.create(
            GSON.toJson(body),
            MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url(API_URL)
            .post(requestBody)
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log("ERROR", "Perspective API HTTP error: " + response.code());
                return apiFailScore();
            }

            JsonObject json = GSON.fromJson(response.body().string(), JsonObject.class);
            JsonObject attributeScores = json.getAsJsonObject("attributeScores");
            if (attributeScores == null) return 0.0;

            double maxScore = 0.0;
            for (String attr : new String[]{
                "TOXICITY","INSULT","THREAT","IDENTITY_ATTACK","SEVERE_TOXICITY"}) {
                if (attributeScores.has(attr)) {
                    double s = attributeScores
                        .getAsJsonObject(attr)
                        .getAsJsonObject("summaryScore")
                        .get("value").getAsDouble();
                    if (s > maxScore) maxScore = s;
                }
            }
            log("API", "Perspective score for user #" + userId + ": " + maxScore);
            return maxScore;

        } catch (IOException e) {
            log("ERROR", "Perspective API network error: " + e.getMessage());
            return apiFailScore();
        }
    }

    /**
     * Score returned on API failure.
     * DEV_MODE=true  → 0.0 (allow, don't block users during development)
     * DEV_MODE=false → 1.0 (block, production safety)
     */
    private static double apiFailScore() {
        if (DEV_MODE) {
            log("DEV", "API failure — DEV_MODE=true, content allowed.");
            return 0.0;
        } else {
            log("PROD", "API failure — DEV_MODE=false, content blocked.");
            return 1.0;
        }
    }

    private static ModerationResult block(int userId, String reason, double score) {
        totalBlocked++;
        totalWarnings++;
        int warnings = USER_WARNINGS.merge(userId, 1, Integer::sum);
        return new ModerationResult(false, reason, score, warnings);
    }

    private static boolean isExcessiveCaps(String text) {
        if (text.length() < 10) return false;
        long upper   = text.chars().filter(Character::isUpperCase).count();
        long letters = text.chars().filter(Character::isLetter).count();
        return letters > 0 && (double) upper / letters > 0.60;
    }

    private static void log(String level, String msg) {
        System.out.println("[ModerationService][" + level + "] " + msg);
    }
}
