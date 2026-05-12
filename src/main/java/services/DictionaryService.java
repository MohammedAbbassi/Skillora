package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour interagir avec les APIs de dictionnaires (EN, Wiktionary, WiktAPI).
 */
public class DictionaryService {

    private static final String EN_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";
    private final HttpClient client;
    private final Gson gson;

    public DictionaryService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public CompletableFuture<String> getEnglishDefinition(String word) {
        if (word == null || word.trim().isEmpty()) return CompletableFuture.completedFuture("Entrez un mot.");
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(EN_API_URL + word.trim().toLowerCase())).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 404) return "Non trouvé (EN).";
                    try {
                        JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);
                        if (jsonArray.size() > 0) {
                            JsonArray meanings = jsonArray.get(0).getAsJsonObject().getAsJsonArray("meanings");
                            if (meanings != null && meanings.size() > 0) {
                                return meanings.get(0).getAsJsonObject().getAsJsonArray("definitions").get(0).getAsJsonObject().get("definition").getAsString();
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    return "Erreur (EN).";
                });
    }

    public CompletableFuture<String> getFrenchDefinition(String word) {
        if (word == null || word.trim().isEmpty()) return CompletableFuture.completedFuture("Entrez un mot.");
        String url = "https://fr.wiktionary.org/w/api.php?action=query&titles=" + word.trim().toLowerCase() + "&prop=extracts&format=json&exintro=1&explaintext=1";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        JsonObject root = gson.fromJson(response.body(), JsonObject.class);
                        JsonObject pages = root.getAsJsonObject("query").getAsJsonObject("pages");
                        for (String key : pages.keySet()) {
                            JsonObject page = pages.getAsJsonObject(key);
                            if (page.has("extract")) {
                                String extract = page.get("extract").getAsString();
                                return extract.isEmpty() ? "Aucun extrait (FR)." : extract;
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    return "Non trouvé (FR).";
                });
    }

    public CompletableFuture<String> getWiktApiDefinition(String word) {
        if (word == null || word.trim().isEmpty()) return CompletableFuture.completedFuture("Entrez un mot.");
        String url = "https://wiktapi.dev/api/v1/word/fr/" + word.trim().toLowerCase();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        JsonObject root = gson.fromJson(response.body(), JsonObject.class);
                        if (root.has("definitions")) {
                            JsonArray defs = root.getAsJsonArray("definitions");
                            if (defs.size() > 0) {
                                return defs.get(0).getAsJsonObject().get("definition").getAsString();
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    return "Non trouvé (WiktAPI).";
                });
    }
}
