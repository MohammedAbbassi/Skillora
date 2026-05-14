package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * TranslationService — Multi-provider translation for Skillora.
 *
 * Supports: Arabic (ar), French (fr), English (en).
 *
 * Provider chain (tries in order until one succeeds):
 *  1. MyMemory API       — free, no key, very reliable, supports AR/FR/EN
 *  2. LibreTranslate     — open-source mirror (translate.fedilab.app)
 *  3. Argos mirror       — second LibreTranslate mirror
 *
 * Features:
 *  - Real HTTP calls — no mock responses
 *  - In-memory cache — same text+lang never calls API twice
 *  - Background thread safe — all methods are static and thread-safe
 *  - Graceful fallback — returns original text if all providers fail
 */
public class TranslationService {

    // ── Language constants ─────────────────────────────────────
    public static final String LANG_FR   = "fr";
    public static final String LANG_EN   = "en";
    public static final String LANG_AR   = "ar";
    public static final String LANG_AUTO = "auto";

    // ── HTTP client ────────────────────────────────────────────
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
        .connectTimeout(8,  TimeUnit.SECONDS)
        .readTimeout(15,    TimeUnit.SECONDS)
        .build();

    private static final Gson GSON = new Gson();

    // ── Translation cache ──────────────────────────────────────
    // Key: "text|source|target"  →  Value: translated string
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    // ═════════════════════════════════════════════════════════
    //  PUBLIC API
    // ═════════════════════════════════════════════════════════

    /**
     * Translates text from sourceLang to targetLang.
     *
     * @param text       text to translate
     * @param sourceLang "fr", "en", "ar", or "auto"
     * @param targetLang "fr", "en", or "ar"
     * @return translated text, or original text if all providers fail
     */
    public static String translateText(String text, String sourceLang, String targetLang) {
        if (text == null || text.isBlank()) return text;
        // Nothing to do if same language
        if (!LANG_AUTO.equals(sourceLang) && sourceLang.equals(targetLang)) return text;

        // ── Cache lookup ───────────────────────────────────────
        String key = text + "|" + sourceLang + "|" + targetLang;
        String cached = CACHE.get(key);
        if (cached != null) {
            log("CACHE", "Hit: " + preview(text));
            return cached;
        }

        // ── Provider chain ─────────────────────────────────────
        String result = tryMyMemory(text, sourceLang, targetLang);

        if (result == null) {
            log("FALLBACK", "MyMemory failed, trying LibreTranslate mirror 1...");
            result = tryLibreTranslate("https://translate.fedilab.app/translate",
                text, sourceLang, targetLang);
        }

        if (result == null) {
            log("FALLBACK", "Mirror 1 failed, trying mirror 2...");
            result = tryLibreTranslate("https://translate.argosopentech.com/translate",
                text, sourceLang, targetLang);
        }

        if (result != null && !result.isBlank()) {
            CACHE.put(key, result);
            log("OK", sourceLang + " → " + targetLang + ": " + preview(result));
            return result;
        }

        // All providers failed — return original text
        log("WARN", "All providers failed — returning original text.");
        return text;
    }

    /** Clears the translation cache. */
    public static void clearCache() {
        CACHE.clear();
        log("CACHE", "Cleared.");
    }

    // ═════════════════════════════════════════════════════════
    //  PROVIDER 1 — MyMemory (most reliable, no key needed)
    // ═════════════════════════════════════════════════════════

    /**
     * MyMemory REST API — GET request with query parameters.
     *
     * Endpoint: https://api.mymemory.translated.net/get
     * Params:   q=text&langpair=fr|en
     * Response: {"responseData": {"translatedText": "..."}, "responseStatus": 200}
     *
     * Language pair format: "source|target"
     * For auto-detect: use "autodetect|target"
     */
    private static String tryMyMemory(String text, String source, String target) {
        try {
            // MyMemory uses "autodetect" for auto, not "auto"
            String srcCode = LANG_AUTO.equals(source) ? "autodetect" : source;
            String langPair = srcCode + "|" + target;

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get"
                + "?q=" + encodedText
                + "&langpair=" + langPair;

            Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();

            try (Response response = HTTP.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log("MYMEMORY", "HTTP " + response.code());
                    return null;
                }

                String body = response.body().string();
                log("MYMEMORY", "Response: " + preview(body));

                JsonObject json = GSON.fromJson(body, JsonObject.class);

                // Check status
                int status = json.has("responseStatus")
                    ? json.get("responseStatus").getAsInt() : 0;
                if (status != 200) {
                    log("MYMEMORY", "Status " + status + " — " +
                        (json.has("responseDetails") ? json.get("responseDetails").getAsString() : ""));
                    return null;
                }

                // Extract translated text
                if (json.has("responseData")) {
                    JsonObject data = json.getAsJsonObject("responseData");
                    if (data.has("translatedText")) {
                        String translated = data.get("translatedText").getAsString();
                        // MyMemory sometimes returns "PLEASE SELECT TWO DISTINCT LANGUAGES"
                        if (translated.startsWith("PLEASE SELECT")) return null;
                        return translated;
                    }
                }
                return null;
            }
        } catch (IOException e) {
            log("MYMEMORY", "Network error: " + e.getMessage());
            return null;
        }
    }

    // ═════════════════════════════════════════════════════════
    //  PROVIDER 2 & 3 — LibreTranslate mirrors (POST JSON)
    // ═════════════════════════════════════════════════════════

    /**
     * LibreTranslate-compatible mirror — POST JSON body.
     *
     * Body: {"q": "text", "source": "fr", "target": "en", "format": "text"}
     * Response: {"translatedText": "..."}
     */
    private static String tryLibreTranslate(String apiUrl, String text,
                                            String source, String target) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("q",      text);
            body.addProperty("source", LANG_AUTO.equals(source) ? "auto" : source);
            body.addProperty("target", target);
            body.addProperty("format", "text");
            // Some mirrors need an empty api_key field
            body.addProperty("api_key", "");

            RequestBody requestBody = RequestBody.create(
                GSON.toJson(body),
                MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .build();

            try (Response response = HTTP.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log("LIBRETRANSLATE", "HTTP " + response.code() + " from " + apiUrl);
                    return null;
                }

                String responseBody = response.body().string();
                log("LIBRETRANSLATE", "Response: " + preview(responseBody));

                JsonObject json = GSON.fromJson(responseBody, JsonObject.class);

                if (json.has("translatedText")) {
                    return json.get("translatedText").getAsString();
                }
                if (json.has("error")) {
                    log("LIBRETRANSLATE", "API error: " + json.get("error").getAsString());
                }
                return null;
            }
        } catch (IOException e) {
            log("LIBRETRANSLATE", "Network error: " + e.getMessage());
            return null;
        }
    }

    // ═════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════

    private static String preview(String s) {
        if (s == null) return "null";
        return s.length() > 80 ? s.substring(0, 80) + "..." : s;
    }

    private static void log(String level, String msg) {
        System.out.println("[TranslationService][" + level + "] " + msg);
    }
}
